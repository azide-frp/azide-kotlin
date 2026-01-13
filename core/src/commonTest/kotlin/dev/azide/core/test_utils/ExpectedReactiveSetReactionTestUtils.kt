package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex.SetChange
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

object ExpectedReactiveSetReactionTestUtils {
    fun <ElementT> expectChange(
        expectedNewElements: Set<ElementT>,
    ): ExpectedTestSubjectReaction<TestReactiveSetObserver<ElementT>> =
        object : ExpectedTestSubjectReaction<TestReactiveSetObserver<ElementT>> {
            override fun prepareDeltaVerifier(
                propagationContext: Transactions.PropagationContext,
                subjectProxy: TestReactiveSetObserver<ElementT>,
            ): ExpectedTestSubjectReaction.DeltaVerifier {
                val oldContent = subjectProxy.getOldContentCopy(
                    propagationContext = propagationContext,
                )

                val expectedAddedElements: Set<ElementT> = expectedNewElements - oldContent
                val expectedRemovedElements: Set<ElementT> = oldContent - expectedNewElements

                subjectProxy.resetReceivedChanges()

                return object : ExpectedTestSubjectReaction.DeltaVerifier {
                    override fun verifyExposedCorrectly() {
                        val exposedOngoingChange = assertNotNull(
                            actual = subjectProxy.observedReactiveSetVertex.ongoingChange,
                            message = "Expected an ongoing change on the subject reactive set vertex.",
                        )

                        verifyChange(
                            change = exposedOngoingChange,
                        )
                    }

                    override fun verifyPropagatedCorrectly() {
                        val receivedChanges = subjectProxy.getAndResetReceivedChanges()

                        val lastReceivedChange = assertNotNull(
                            actual = receivedChanges.lastOrNull(),
                            message = "Expected at least one change to be propagated.",
                        )

                        verifyChange(
                            change = lastReceivedChange,
                        )
                    }

                    private fun verifyChange(
                        change: SetChange<ElementT>,
                    ) {
                        assertEquals(
                            expected = change.addedElements,
                            actual = expectedAddedElements,
                            message = "Added elements do not match expected added elements.",
                        )

                        assertEquals(
                            expected = change.removedElements,
                            actual = expectedRemovedElements,
                            message = "Removed elements do not match expected removed elements.",
                        )
                    }
                }
            }

            override fun prepareNewStateVerifier(
                propagationContext: Transactions.PropagationContext,
                subjectProxy: TestReactiveSetObserver<ElementT>,
            ): ExpectedTestSubjectReaction.NewStateVerifier<TestReactiveSetObserver<ElementT>> {
                return object : ExpectedTestSubjectReaction.NewStateVerifier<TestReactiveSetObserver<ElementT>> {
                    override fun verifyNewState(
                        propagationContext: Transactions.PropagationContext,
                    ) {
                        val newContent = subjectProxy.getOldContentCopy(
                            propagationContext = propagationContext,
                        )

                        assertEquals(
                            expected = expectedNewElements,
                            actual = newContent,
                            message = "Expected content to match new elements after change.",
                        )
                    }
                }
            }
        }

    fun <ElementT> expectNoReaction(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedTestSubjectReaction<TestReactiveSetObserver<ElementT>> =
        object : ExpectedTestSubjectReaction<TestReactiveSetObserver<ElementT>> {
            override fun prepareDeltaVerifier(
                propagationContext: Transactions.PropagationContext,
                subjectProxy: TestReactiveSetObserver<ElementT>,
            ): ExpectedTestSubjectReaction.DeltaVerifier {
                subjectProxy.resetReceivedChanges()

                return object : ExpectedTestSubjectReaction.DeltaVerifier {
                    override fun verifyExposedCorrectly() {
                        val ongoingChange = subjectProxy.observedReactiveSetVertex.ongoingChange

                        assertNull(
                            ongoingChange,
                            message = "Expected no ongoing change on the subject reactive set vertex.",
                        )
                    }

                    override fun verifyPropagatedCorrectly() {
                        when (intermediatePropagationTolerance) {
                            IntermediatePropagationTolerance.DoNotTolerate -> {
                                val receivedChanges = subjectProxy.getAndResetReceivedChanges()

                                assertEquals(
                                    expected = emptyList(),
                                    actual = receivedChanges,
                                    message = "Expected no changes to be propagated.",
                                )
                            }

                            IntermediatePropagationTolerance.Tolerate -> {
                                val lastReceivedChange = subjectProxy.getAndResetReceivedChanges().lastOrNull()

                                assertNull(
                                    actual = lastReceivedChange,
                                    message = "Expected the last propagated change to be null (no change or change revocation).",
                                )
                            }
                        }
                    }
                }
            }

            override fun prepareNewStateVerifier(
                propagationContext: Transactions.PropagationContext,
                subjectProxy: TestReactiveSetObserver<ElementT>,
            ): ExpectedTestSubjectReaction.NewStateVerifier<TestReactiveSetObserver<ElementT>> {
                val originalContent = subjectProxy.getOldContentCopy(
                    propagationContext = propagationContext,
                )

                return object : ExpectedTestSubjectReaction.NewStateVerifier<TestReactiveSetObserver<ElementT>> {
                    override fun verifyNewState(
                        propagationContext: Transactions.PropagationContext,
                    ) {
                        val newContent = subjectProxy.getOldContentCopy(
                            propagationContext = propagationContext,
                        )

                        assertEquals(
                            expected = originalContent,
                            actual = newContent,
                            message = "Expected content to remain unchanged.",
                        )
                    }
                }
            }
        }
}
