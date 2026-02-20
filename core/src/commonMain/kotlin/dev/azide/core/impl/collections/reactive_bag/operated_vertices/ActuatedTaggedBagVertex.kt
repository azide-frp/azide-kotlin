package dev.azide.core.impl.collections.reactive_bag.operated_vertices

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.collections.reactive_bag.MutableTaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_bag.abstract_vertices.AbstractStatefulTrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_bag.mapKeepingTags
import dev.azide.core.impl.collections.reactive_bag.mapToKeepingTags
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.impl.effects.InternalEffect
import dev.azide.core.impl.enqueueForCommitment
import dev.azide.core.impl.registerBoundListenerOnline

class ActuatedTaggedBagVertex<InnerResultT> private constructor(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceEffectBag: ReactiveBag<Effect<InnerResultT>>,
    initialInnerEffectOutcomes: TaggedBag<Effect.Outcome<InnerResultT>>,
) : AbstractStatefulTrackedTaggedBagVertex<InnerResultT>(
    wrapUpContext = wrapUpContext,
    initialTaggedElements = initialInnerEffectOutcomes.mapToKeepingTags(MutableTaggedBag.empty()) { it.result },
), Vertex.BoundListener, CommittableVertex {
    class ActuationEffect<InnerResultT>(
        private val sourceEffectBag: ReactiveBag<Effect<InnerResultT>>,
    ) : InternalEffect<ReactiveBag<InnerResultT>> {
        override fun startInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): InternalEffect.RevocableOutcome<ReactiveBag<InnerResultT>> {
            val initialInnerEffects: TaggedBag<Effect<InnerResultT>> = sourceEffectBag.trackedVertex.getOldContentView(
                propagationContext = propagationContext,
            )

            val initialInnerEffectStartOutcomes: TaggedBag<Action.Outcome<Effect.Outcome<InnerResultT>>> =
                initialInnerEffects.mapKeepingTags { initialInnerEffect: Effect<InnerResultT> ->
                    initialInnerEffect.start.executeInternally(
                        propagationContext = propagationContext,
                        wrapUpContext = wrapUpContext,
                    )
                }

            val initialInnerEffectOutcomes: TaggedBag<Effect.Outcome<InnerResultT>> =
                initialInnerEffectStartOutcomes.mapKeepingTags { it.result }

            return with(
                ActuatedTaggedBagVertex(
                    wrapUpContext = wrapUpContext,
                    sourceEffectBag = sourceEffectBag,
                    initialInnerEffectOutcomes = initialInnerEffectOutcomes,
                ),
            ) {
                object : InternalEffect.RevocableOutcome<ReactiveBag<InnerResultT>> {
                    override val result = ReactiveBag.Ordinary(
                        trackedVertex = this@with,
                    )

                    /**
                     * Cancel the reactive bag actuation effect.
                     */
                    override fun cancelInternally(
                        propagationContext: Transactions.PropagationContext,
                        wrapUpContext: Transactions.WrapUpContext,
                    ): Revocable {
                        shutDown()

                        // Revoke the ongoing change (if any)
                        if (ongoingChange != null) {
                            exposeChangeNotifyingListeners(
                                propagationContext = propagationContext,
                                change = null,
                            )
                        }

                        // Cancel all stable inner effects
                        val stableInnerEffectCancellationRevocables =
                            stableInnerEffectHandles.map { innerEffectHandle ->
                                innerEffectHandle.cancel.executeInternally(
                                    propagationContext = propagationContext,
                                    wrapUpContext = wrapUpContext,
                                ).revocable
                            }

                        return object : Revocable {
                            /**
                             * Revoke the cancellation of the reactive bag actuation effect.
                             */
                            override fun revoke() {
                                if (internalState == InternalState.Disposed) {
                                    return
                                }

                                // Revoke the cancellation of all stable inner effects
                                stableInnerEffectCancellationRevocables.forEach { stableInnerEffectCancellationOutcome ->
                                    stableInnerEffectCancellationOutcome.revoke()
                                }

                                // Re-initialize the effect
                                val startUpChange = startUp(
                                    propagationContext = propagationContext,
                                )

                                exposeChangeNotifyingListeners(
                                    propagationContext = propagationContext,
                                    change = startUpChange,
                                )
                            }
                        }
                    }

                    /**
                     * Revoke the start of the actuation effect.
                     */
                    override fun revoke() {
                        initialInnerEffectStartOutcomes.forEach { initialInnerEffectStartOutcome ->
                            initialInnerEffectStartOutcome.revocable.revoke()
                        }

                        if (internalState == InternalState.StartedUp) {
                            shutDown()
                        }

                        internalState = InternalState.Disposed
                    }
                }
            }
        }
    }

    private enum class InternalState {
        ShutDown, StartedUp, Disposed,
    }

    private val sourceVertex: TrackedTaggedBagVertex<Effect<InnerResultT>>
        get() = sourceEffectBag.trackedVertex

    private val stableInnerEffectHandles: MutableTaggedBag<Effect.Handle> =
        initialInnerEffectOutcomes.mapToKeepingTags(MutableTaggedBag.empty()) {
            it.handle
        }

    private var internalState = InternalState.ShutDown

    private var upstreamListenerHandle: Vertex.ListenerHandle? = null

    private var unstableInnerEffectCancellationRevocableByTag: MutableMap<ReactiveBag.Tag, Revocable>? = null

    private var unstableNewInnerEffectStartOutcomeByTag: MutableMap<ReactiveBag.Tag, Action.Outcome<Effect.Outcome<InnerResultT>>>? =
        null

    private var isEnqueuedForCommitment = false

    /**
     * Handle the source effect bag change
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        val unstableInnerEffectCancellationRevocableByTag = this.unstableInnerEffectCancellationRevocableByTag
            ?: throw IllegalStateException("Effect doesn't seem to be ongoing")

        val unstableNewInnerEffectStartOutcomeByTag = this.unstableNewInnerEffectStartOutcomeByTag
            ?: throw IllegalStateException("Effect doesn't seem to be ongoing")

        val sourceOngoingChange: TaggedBagChange<Effect<InnerResultT>>? = sourceVertex.ongoingChange

        when (sourceOngoingChange) {
            null -> { // Source change revocation
                // Revoke all ongoing inner effect cancellations
                unstableInnerEffectCancellationRevocableByTag.forEach { (_, it) ->
                    it.revoke()
                }

                unstableInnerEffectCancellationRevocableByTag.clear()

                // Revoke all ongoing new inner effect starts
                unstableNewInnerEffectStartOutcomeByTag.forEach { (_, it) ->
                    it.revocable.revoke()
                }

                unstableNewInnerEffectStartOutcomeByTag.clear()

                exposeChangeNotifyingListeners(
                    propagationContext = propagationContext,
                    change = null,
                )
            }

            else -> { // Initial source change / source change correction
                sourceOngoingChange.removedTags.forEach { removedTag: ReactiveBag.Tag ->
                    // If the respective inner effect isn't already being cancelled, cancel it
                    unstableInnerEffectCancellationRevocableByTag.getOrPut(removedTag) {
                        val removedStableInnerEffectHandle = stableInnerEffectHandles.getByTag(removedTag)
                            ?: throw IllegalStateException("Removed tag $removedTag has no associated inner effect handle")

                        removedStableInnerEffectHandle.cancel.executeInternallyWrappedUp(
                            propagationContext = propagationContext,
                        ).revocable
                    }
                }

                // Revoke inner effect cancellations for effects that are no longer being removed
                unstableInnerEffectCancellationRevocableByTag.entries.removeAll {
                        (
                            previouslyRemovedTag: ReactiveBag.Tag,
                            previousInnerEffectCancellationRevocable: Revocable,
                        ),
                    ->
                    when (previouslyRemovedTag) {
                        in sourceOngoingChange.removedTags -> {
                            false // (Don't remove)
                        }

                        else -> {
                            previousInnerEffectCancellationRevocable.revoke() // Revoke previous cancellation
                            true // (Remove)
                        }
                    }
                }

                // Process the new inner effects
                val addedInnerResultByTag = mutableMapOf<ReactiveBag.Tag, InnerResultT>()
                val replacedInnerResultByTag = mutableMapOf<ReactiveBag.Tag, InnerResultT>()

                for ((changedTag: ReactiveBag.Tag, newInnerEffect: Effect<InnerResultT>) in sourceOngoingChange.changedElementByTag) {
                    val changedStableEffectOutcome: Effect.Handle? =
                        stableInnerEffectHandles.getByTag(tag = changedTag)

                    if (changedStableEffectOutcome != null) { // Inner effect replacement
                        // Cancel the stable replaced effect if it's not already being cancelled
                        unstableInnerEffectCancellationRevocableByTag.getOrPut(changedTag) {
                            changedStableEffectOutcome.cancel.executeInternallyWrappedUp(
                                propagationContext = propagationContext,
                            ).revocable
                        }
                    }

                    val freshNewInnerEffectStartOutcome = newInnerEffect.start.executeInternallyWrappedUp(
                        propagationContext = propagationContext,
                    )

                    val previousNewInnerEffectStartOutcome: Action.Outcome<Effect.Outcome<InnerResultT>>? =
                        unstableNewInnerEffectStartOutcomeByTag.put(
                            changedTag,
                            freshNewInnerEffectStartOutcome,
                        )

                    // If the previous change revision caused a new inner effect to be started for this tag, revoke it
                    previousNewInnerEffectStartOutcome?.revocable?.revoke()

                    val result = freshNewInnerEffectStartOutcome.result.result

                    if (changedStableEffectOutcome != null) {
                        replacedInnerResultByTag[changedTag] = result
                    } else {
                        addedInnerResultByTag[changedTag] = result
                    }
                }

                // Revoke unstable new inner effect starts for effects that are no longer being changed in this revision
                unstableNewInnerEffectStartOutcomeByTag.entries.removeAll {
                        (
                            previouslyChangedTag: ReactiveBag.Tag,
                            previousEffectStartOutcome: Action.Outcome<Effect.Outcome<InnerResultT>>,
                        ),
                    ->
                    when (previouslyChangedTag) {
                        in sourceOngoingChange.changedElementByTag -> {
                            false // (Don't remove)
                        }

                        else -> {
                            previousEffectStartOutcome.revocable.revoke() // Revoke previous start
                            true // (Remove)
                        }
                    }
                }

                exposeChangeNotifyingListeners(
                    propagationContext = propagationContext,
                    change = TaggedBagChange(
                        addedElementByTag = addedInnerResultByTag,
                        replacedElementByTag = replacedInnerResultByTag,
                        removedTags = sourceOngoingChange.removedTags,
                    ),
                )
            }
        }

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    override fun commit() {
        if (internalState != InternalState.StartedUp) {
            return
        }

        val unstableInnerEffectCancellationRevocableByTag = this.unstableInnerEffectCancellationRevocableByTag!!
        val unstableNewInnerEffectStartOutcomeByTag = this.unstableNewInnerEffectStartOutcomeByTag!!

        unstableInnerEffectCancellationRevocableByTag.forEach { (removedEffectTag: ReactiveBag.Tag, _) ->
            stableInnerEffectHandles.removeByTag(removedEffectTag)
                ?: throw IllegalStateException("Inconsistent internal state: Removed effect tag $removedEffectTag has no associated inner effect handle")
        }

        unstableNewInnerEffectStartOutcomeByTag.forEach { (changedEffectTag, changedEffect) ->
            stableInnerEffectHandles.addByTag(changedEffectTag, changedEffect.result.handle)
        }

        // FIXME: These lines are necessary, but removing them doesn't cause any of the tests to fail
        unstableInnerEffectCancellationRevocableByTag.clear()
        unstableNewInnerEffectStartOutcomeByTag.clear()

        isEnqueuedForCommitment = false
    }

    override fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): TaggedBagChange<InnerResultT>? = startUp(
        propagationContext = propagationContext,
    )

    private fun startUp(
        propagationContext: Transactions.PropagationContext,
    ): TaggedBagChange<InnerResultT>? {
        if (internalState != InternalState.ShutDown) {
            throw IllegalStateException("Effect is already started up or has been revoked: $internalState")
        }

        if (this@ActuatedTaggedBagVertex.upstreamListenerHandle != null || this@ActuatedTaggedBagVertex.unstableInnerEffectCancellationRevocableByTag != null || this@ActuatedTaggedBagVertex.unstableNewInnerEffectStartOutcomeByTag != null) {
            throw IllegalStateException("Vertex seems to already be started up")
        }

        // Re-register the listener
        this@ActuatedTaggedBagVertex.upstreamListenerHandle = sourceVertex.registerBoundListenerOnline(
            propagationContext = propagationContext,
            listener = this@ActuatedTaggedBagVertex,
        )

        val sourceOngoingChange: TaggedBagChange<Effect<InnerResultT>>? = sourceVertex.ongoingChange

        val startUpChange = when (sourceOngoingChange) {
            null -> { // There's no ongoing source effect change
                this@ActuatedTaggedBagVertex.unstableInnerEffectCancellationRevocableByTag = mutableMapOf()
                this@ActuatedTaggedBagVertex.unstableNewInnerEffectStartOutcomeByTag = mutableMapOf()

                null
            }

            else -> { // There's an ongoing source effect change
                // Cancel all stable effects that are being removed from the bag
                val initialInnerEffectCancellationRevocableByTag =
                    sourceOngoingChange.removedTags.associateWithTo(mutableMapOf()) { removedTag ->
                        val removedStableInnerEffectHandle = stableInnerEffectHandles.getByTag(removedTag)
                            ?: throw IllegalStateException("Removed tag $removedTag has no associated inner effect handle")

                        removedStableInnerEffectHandle.cancel.executeInternallyWrappedUp(
                            propagationContext = propagationContext,
                        ).revocable
                    }

                // Start all new inner effects that are being added to the bag or replace the previous effect
                val initialAddedEffectTags = mutableSetOf<ReactiveBag.Tag>()
                val initialReplacedEffectTags = mutableSetOf<ReactiveBag.Tag>()

                val initialNewInnerEffectStartOutcomeByTag =
                    sourceOngoingChange.changedElementByTag.mapValuesTo(mutableMapOf()) { (changedTag, newInnerEffect) ->
                        val changedStableEffectHandle = stableInnerEffectHandles.getByTag(tag = changedTag)

                        if (changedStableEffectHandle != null) { // Inner effect replacement
                            // Cancel the stable replaced effect
                            initialInnerEffectCancellationRevocableByTag[changedTag] = changedStableEffectHandle.cancel.executeInternallyWrappedUp(
                                propagationContext = propagationContext,
                            ).revocable

                            initialReplacedEffectTags.add(changedTag)
                        } else {
                            initialAddedEffectTags.add(changedTag)
                        }

                        newInnerEffect.start.executeInternallyWrappedUp(
                            propagationContext = propagationContext,
                        )
                    }

                this@ActuatedTaggedBagVertex.unstableInnerEffectCancellationRevocableByTag =
                    initialInnerEffectCancellationRevocableByTag

                this@ActuatedTaggedBagVertex.unstableNewInnerEffectStartOutcomeByTag =
                    initialNewInnerEffectStartOutcomeByTag

                TaggedBagChange(
                    addedElementByTag = initialNewInnerEffectStartOutcomeByTag.filterKeys { it in initialAddedEffectTags }
                        .mapValues { (_, outcome) -> outcome.result.result },
                    replacedElementByTag = initialNewInnerEffectStartOutcomeByTag.filterKeys { it in initialReplacedEffectTags }
                        .mapValues { (_, outcome) -> outcome.result.result },
                    removedTags = sourceOngoingChange.removedTags,
                )
            }
        }

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )

        internalState = InternalState.StartedUp

        return startUpChange
    }

    private fun shutDown() {
        if (internalState != InternalState.StartedUp) {
            throw IllegalStateException("Effect is not started up: $internalState")
        }

        run {
            val upstreamListenerHandle =
                this.upstreamListenerHandle ?: throw IllegalStateException("Effect doesn't seem to be started up")

            this.upstreamListenerHandle = null

            // Unregister the listener
            sourceVertex.unregisterListener(
                handle = upstreamListenerHandle,
            )
        }

        run {
            val unstableInnerEffectCancellationRevocables = this.unstableInnerEffectCancellationRevocableByTag
                ?: throw IllegalStateException("Effect doesn't seem to be started up")

            this.unstableInnerEffectCancellationRevocableByTag = null


            // Revoke all ongoing unstable inner effect cancellations
            unstableInnerEffectCancellationRevocables.forEach { (_, it) ->
                it.revoke()
            }
        }

        run {
            val unstableNewInnerEffectStartOutcomes = this.unstableNewInnerEffectStartOutcomeByTag
                ?: throw IllegalStateException("Effect doesn't seem to be started up")

            this.unstableNewInnerEffectStartOutcomeByTag = null

            // Revoke all ongoing unstable new inner effect starts
            unstableNewInnerEffectStartOutcomes.forEach { (_, it) ->
                it.revocable.revoke()
            }
        }

        internalState = InternalState.ShutDown
    }

    private fun ensureEnqueuedForCommitment(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (!isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            isEnqueuedForCommitment = true
        }
    }
}
