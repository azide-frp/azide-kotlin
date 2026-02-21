package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.event_stream.EventStreamVertex.Emission
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListenerOnline

class ExecutedEachEventStreamVertex<EventT>(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceEventStream: EventStream<Action<EventT>>,
) : AbstractStatefulEventStreamVertex<EventT>(
    wrapUpContext = wrapUpContext,
), BoundListener {
    class ExecutionEffect<EventT>(
        private val sourceEventStream: EventStream<Action<EventT>>,
    ) : InternalEffect<EventStream<EventT>> {
        override fun startInternally(
            propagationContext: PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): InternalEffect.RevocableOutcome<EventStream<EventT>> = with(
            ExecutedEachEventStreamVertex(
                wrapUpContext = wrapUpContext,
                sourceEventStream = sourceEventStream,
            ),
        ) {
            object : InternalEffect.RevocableOutcome<EventStream<EventT>> {
                override val result = EventStream.Ordinary(
                    vertex = this@with,
                )

                /**
                 * Cancel the execution effect.
                 */
                override fun cancelInternally(
                    propagationContext: PropagationContext,
                    wrapUpContext: Transactions.WrapUpContext,
                ): Revocable {
                    detach(
                    )

                    if (ongoingEmission != null) {
                        exposeEmissionNotifyingListeners(
                            propagationContext = propagationContext,
                            emission = null,
                        )
                    }

                    return object : Revocable {
                        /**
                         * Revoke the cancellation of the execution effect.
                         */
                        override fun revoke() {
                            if (internalState == InternalState.Disposed) {
                                return
                            }

                            val attachEmission = attach(
                                propagationContext = propagationContext,
                            )

                            exposeEmissionNotifyingListeners(
                                propagationContext = propagationContext,
                                emission = attachEmission,
                            )
                        }
                    }
                }

                /**
                 * Revoke the start of the execution effect.
                 */
                override fun revoke() {
                    if (internalState == InternalState.Attached) {
                        detach(
                        )
                    }

                    internalState = InternalState.Disposed
                }
            }
        }
    }

    private enum class InternalState {
        Detached, Attached, Disposed,
    }

    private var internalState = InternalState.Detached

    private var upstreamListenerHandle: ListenerHandle? = null

    private var executedActionRevocable: Revocable? = null

    /**
     * Handle the emission of the source action event stream vertex.
     */
    override fun handle(
        propagationContext: PropagationContext,
    ) {
        val sourceEventStream = this@ExecutedEachEventStreamVertex.sourceEventStream

        when (val emission = sourceEventStream.vertex.ongoingEmission) {
            null -> {
                val executedActionRevocable =
                    this.executedActionRevocable ?: throw AssertionError("There's no record of the revoked action")

                executedActionRevocable.revoke()
                this.executedActionRevocable = null

                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                this.executedActionRevocable?.revoke()

                val emittedAction: Action<EventT> = emission.emittedEvent

                val (emittedEvent: EventT, executedActionRevocable) = emittedAction.executeInternallyWrappedUpUnpacked(
                    propagationContext = propagationContext,
                )

                this.executedActionRevocable = executedActionRevocable

                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = Emission(
                        emittedEvent = emittedEvent,
                    ),
                )
            }
        }
    }

    private fun attach(
        propagationContext: PropagationContext,
    ): Emission<EventT>? {
        if (internalState != InternalState.Detached) {
            throw IllegalStateException("ListenableVertex is attached or disposed: $internalState")
        }

        val sourceEventStream = this@ExecutedEachEventStreamVertex.sourceEventStream

        val sourceVertex = sourceEventStream.vertex

        // Attach to the source event stream
        this@ExecutedEachEventStreamVertex.upstreamListenerHandle = sourceVertex.registerBoundListenerOnline(
            propagationContext = propagationContext,
            listener = this@ExecutedEachEventStreamVertex,
        )

        val attachChange = sourceVertex.ongoingEmission?.let { sourceOngoingActionEmission ->
            val emittedAction: Action<EventT> = sourceOngoingActionEmission.emittedEvent

            val (event: EventT, executedActionRevocable: Revocable) = emittedAction.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            this.executedActionRevocable = executedActionRevocable

            Emission(
                emittedEvent = event,
            )
        }

        internalState = InternalState.Attached

        return attachChange
    }

    private fun detach() {
        if (internalState != InternalState.Attached) {
            throw IllegalStateException("ListenableVertex is already detached or disposed: $internalState")
        }

        val sourceEventStream = this@ExecutedEachEventStreamVertex.sourceEventStream

        val upstreamListenerHandle = this@ExecutedEachEventStreamVertex.upstreamListenerHandle
            ?: throw IllegalStateException("It seems as if the vertex wasn't wrapped up properly or is already cancelled")

        this@ExecutedEachEventStreamVertex.upstreamListenerHandle = null

        sourceEventStream.vertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        val executedActionRevocable = this@ExecutedEachEventStreamVertex.executedActionRevocable

        if (executedActionRevocable != null) {
            executedActionRevocable.revoke()

            this@ExecutedEachEventStreamVertex.executedActionRevocable = null
        }

        internalState = InternalState.Detached
    }

    override fun transit() {
        this.executedActionRevocable = null
    }

    override fun initialize(
        propagationContext: PropagationContext,
    ): Emission<EventT>? = attach(
        propagationContext = propagationContext,
    )
}
