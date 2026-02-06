package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

interface ExpectedReactiveBagChange<ElementT> : ExpectedTestSubjectReaction<ReactiveBag<ElementT>>

interface ExpectedReactiveBagContent<ElementT> : ExpectedTestSubjectState<ReactiveBag<ElementT>>

interface ExpectedReactiveBagContentTransition<ElementT> : ExpectedTestSubjectTransition<ReactiveBag<ElementT>>

private abstract class AbstractExpectedReactiveBagChange<ElementT> : ExpectedReactiveBagChange<ElementT> {
    final override fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<ReactiveBag<ElementT>>,
    ): ExpectedTestSubjectReaction.TestSubjectReactionVerifier =
        object : ExpectedTestSubjectReaction.TestSubjectReactionVerifier, BoundListener {
            private val subjectVertex: TrackedTaggedBagVertex<ElementT>
                get() = subjectLazy.value.trackedVertex

            private var listenerHandle: ListenerHandle? = null

            private var initialChange: TaggedBagChange<ElementT>? = null

            private val receivedChanges = mutableListOf<TaggedBagChange<ElementT>?>()

            override fun install() {
                if (listenerHandle != null) {
                    throw IllegalStateException("ReactiveBag verifier is already installed")
                }

                listenerHandle = subjectVertex.registerBoundListenerOnline(
                    propagationContext = propagationContext,
                    listener = this,
                )

                initialChange = subjectVertex.ongoingChange
            }

            override fun verifyReaction() {
                if (listenerHandle == null) {
                    throw IllegalStateException("A non-installed verifier cannot be used for verification")
                }

                assertEquals(
                    expected = expectedEffectiveChange,
                    actual = subjectVertex.ongoingChange,
                    message = "Exposed ongoing change did not match the expected change.",
                )

                val effectiveChange = when {
                    receivedChanges.isNotEmpty() -> receivedChanges.last()
                    else -> initialChange
                }

                assertEquals(
                    expected = expectedEffectiveChange,
                    actual = effectiveChange,
                    message = "The effective received change did not match the expected change.",
                )

                when (intermediatePropagationTolerance) {
                    IntermediatePropagationTolerance.DoNotTolerate -> {
                        assertTrue(
                            actual = receivedChanges.size <= 1,
                            message = "Expected at most one change to be propagated, but received ${receivedChanges.size} changes (intermediate propagation is not tolerated).",
                        )
                    }

                    IntermediatePropagationTolerance.Tolerate -> {}
                }
            }

            override fun uninstall() {
                val listenerHandle =
                    this.listenerHandle ?: throw IllegalStateException("Cannot uninstall a non-installed cell verifier")

                subjectVertex.unregisterListener(
                    handle = listenerHandle,
                )

                this.listenerHandle = null
                this.initialChange = null
            }

            override fun handle(
                propagationContext: Transactions.PropagationContext,
            ) {
                receivedChanges.add(subjectVertex.ongoingChange)
            }
        }

    abstract val intermediatePropagationTolerance: IntermediatePropagationTolerance

    abstract val expectedEffectiveChange: TaggedBagChange<ElementT>?
}

abstract class AbstractExpectedReactiveBagContentTransition<ElementT> : ExpectedReactiveBagContentTransition<ElementT> {
    final override val expectedOldState: ExpectedReactiveBagContent<ElementT>
        get() = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
            expectedTaggedContent = expectedOldTaggedContent,
        )

    final override val expectedNewState: ExpectedReactiveBagContent<ElementT>
        get() = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
            expectedTaggedContent = expectedNewTaggedContent,
        )

    abstract val expectedOldTaggedContent: Map<Tag, ElementT>

    abstract val expectedNewTaggedContent: Map<Tag, ElementT>
}

@Suppress("ClassName")
object ReactiveBag_expectations_testUtils {
    fun <ElementT> expectTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldTaggedContent: Map<Tag, ElementT>,
        expectedNewTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> =
        object : AbstractExpectedReactiveBagContentTransition<ElementT>() {
            override val expectedOldTaggedContent: Map<Tag, ElementT> = expectedOldTaggedContent

            override val expectedNewTaggedContent: Map<Tag, ElementT> = expectedNewTaggedContent

            override val expectedReaction: ExpectedTestSubjectReaction<ReactiveBag<ElementT>> =
                object : AbstractExpectedReactiveBagChange<ElementT>() {
                    override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
                        intermediatePropagationTolerance

                    override val expectedEffectiveChange: TaggedBagChange<ElementT>? = TaggedBagChange_testUtils.diff(
                        oldTaggedContent = expectedOldTaggedContent,
                        newTaggedContent = expectedNewTaggedContent,
                    )
                }

        }

    fun <ElementT> expectNoTaggedContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContentTransition<ElementT> =
        object : AbstractExpectedReactiveBagContentTransition<ElementT>() {
            override val expectedOldTaggedContent: Map<Tag, ElementT> = expectedUnaffectedTaggedContent

            override val expectedNewTaggedContent: Map<Tag, ElementT> = expectedUnaffectedTaggedContent

            override val expectedReaction: ExpectedTestSubjectReaction<ReactiveBag<ElementT>> =
                object : AbstractExpectedReactiveBagChange<ElementT>() {
                    override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
                        intermediatePropagationTolerance

                    override val expectedEffectiveChange: TaggedBagChange<ElementT>? = null
                }
        }

    fun <ElementT> expectStableTaggedContent(
        expectedTaggedContent: Map<Tag, ElementT>,
    ): ExpectedReactiveBagContent<ElementT> = object : ExpectedReactiveBagContent<ElementT> {
        override fun verifyStableState(
            propagationContext: Transactions.PropagationContext,
            subject: ReactiveBag<ElementT>,
        ) {
            val actualTaggedContent = subject.trackedVertex.getOldContentView(
                propagationContext = propagationContext,
            ).elementByTag

            assertEquals(
                expected = expectedTaggedContent,
                actual = actualTaggedContent,
                message = "The stable tagged content of the reactive bag did not match the expected stable tagged content.",
            )
        }
    }
}
