package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_collection.TrackedListVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.applyTo
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.ExpectedTestSubjectState
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

interface ExpectedReactiveListChange<ElementT> : ExpectedTestSubjectReaction<ReactiveList<ElementT>>

interface ExpectedReactiveListContent<ElementT> : ExpectedTestSubjectState<ReactiveList<ElementT>>

interface ExpectedReactiveListContentTransition<ElementT> : ExpectedTestSubjectTransition<ReactiveList<ElementT>>

private abstract class AbstractExpectedReactiveListChange<ElementT> : ExpectedReactiveListChange<ElementT> {
    final override fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<ReactiveList<ElementT>>,
    ): ExpectedTestSubjectReaction.TestSubjectReactionVerifier =
        object : ExpectedTestSubjectReaction.TestSubjectReactionVerifier, BoundListener {
            private val subjectVertex: TrackedListVertex<ElementT>
                get() = subjectLazy.value.trackedVertex

            private var listenerHandle: ListenerHandle? = null

            private var initialChange: ListChange<ElementT>? = null

            private val receivedChanges = mutableListOf<ListChange<ElementT>?>()

            override fun install() {
                if (listenerHandle != null) {
                    throw IllegalStateException("ReactiveList verifier is already installed")
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

                verifyEffectiveChange(
                    effectiveChange = subjectVertex.ongoingChange,
                )

                val effectiveReceivedChange = when {
                    receivedChanges.isNotEmpty() -> receivedChanges.last()
                    else -> initialChange
                }

                verifyEffectiveChange(
                    effectiveChange = effectiveReceivedChange,
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

    abstract fun verifyEffectiveChange(
        effectiveChange: ListChange<ElementT>?,
    )

    abstract val intermediatePropagationTolerance: IntermediatePropagationTolerance
}

abstract class AbstractExpectedReactiveListContentTransition<ElementT> :
    ExpectedReactiveListContentTransition<ElementT> {
    final override val expectedOldState: ExpectedReactiveListContent<ElementT>
        get() = ReactiveList_expectations_testUtils.expectStableContent(
            expectedContent = expectedOldContent,
        )

    final override val expectedNewState: ExpectedReactiveListContent<ElementT>
        get() = ReactiveList_expectations_testUtils.expectStableContent(
            expectedContent = expectedNewContent,
        )

    abstract val expectedOldContent: List<ElementT>

    abstract val expectedNewContent: List<ElementT>
}

@Suppress("ClassName")
object ReactiveList_expectations_testUtils {
    fun <ElementT> expectContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldContent: List<ElementT>,
        expectedNewContent: List<ElementT>,
    ): ExpectedReactiveListContentTransition<ElementT> =
        object : AbstractExpectedReactiveListContentTransition<ElementT>() {
            override val expectedOldContent: List<ElementT> = expectedOldContent

            override val expectedNewContent: List<ElementT> = expectedNewContent

            override val expectedReaction: ExpectedTestSubjectReaction<ReactiveList<ElementT>> =
                object : AbstractExpectedReactiveListChange<ElementT>() {
                    override fun verifyEffectiveChange(effectiveChange: ListChange<ElementT>?) {
                        assertNotNull(
                            actual = effectiveChange,
                            message = "Expected a (non-null) effective change, but effectively received no change.",
                        )

                        val effectiveNewContent: List<ElementT> = expectedOldContent.toMutableList().also {
                            effectiveChange.applyTo(it)
                        }

                        assertEquals(
                            expected = expectedNewContent,
                            actual = effectiveNewContent,
                            message = "The effective change did not result in the expected new content.",
                        )
                    }

                    override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
                        intermediatePropagationTolerance
                }
        }

    fun <ElementT> expectNoContentTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedContent: List<ElementT>,
    ): ExpectedReactiveListContentTransition<ElementT> =
        object : AbstractExpectedReactiveListContentTransition<ElementT>() {
            override val expectedOldContent: List<ElementT> = expectedUnaffectedContent

            override val expectedNewContent: List<ElementT> = expectedUnaffectedContent

            override val expectedReaction: ExpectedTestSubjectReaction<ReactiveList<ElementT>> =
                object : AbstractExpectedReactiveListChange<ElementT>() {
                    override fun verifyEffectiveChange(effectiveChange: ListChange<ElementT>?) {
                        assertNull(
                            actual = effectiveChange,
                            message = "Expected no effective change, but received $effectiveChange (intermediate propagation is not tolerated).",
                        )
                    }

                    override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
                        intermediatePropagationTolerance
                }
        }

    fun <ElementT> expectStableContent(
        expectedContent: List<ElementT>,
    ): ExpectedReactiveListContent<ElementT> = object : ExpectedReactiveListContent<ElementT> {
        override fun verifyStableState(
            propagationContext: Transactions.PropagationContext,
            subject: ReactiveList<ElementT>,
        ) {
            val actualContent = subject.trackedVertex.getOldContentView(
                propagationContext = propagationContext,
            )

            assertEquals(
                expected = expectedContent,
                actual = actualContent,
                message = "The stable content of the reactive list did not match the expected stable content.",
            )
        }
    }
}
