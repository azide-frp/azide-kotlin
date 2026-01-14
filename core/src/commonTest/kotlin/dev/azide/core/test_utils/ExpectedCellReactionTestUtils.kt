package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

typealias ExpectedCellReaction<ValueT> = ExpectedTestSubjectReaction<Cell<ValueT>, TestCellObserver<ValueT>>

object ExpectedCellReactionTestUtils {
    fun <ValueT> expectUpdate(
        expectedNewValue: ValueT,
    ): ExpectedCellReaction<ValueT> = object : ExpectedCellReaction<ValueT> {
        override fun prepareDeltaVerifier(
            propagationContext: Transactions.PropagationContext,
            subjectProxy: TestCellObserver<ValueT>,
        ): ExpectedTestSubjectReaction.DeltaVerifier {
            subjectProxy.resetReceivedUpdates()

            val expectedUpdate = CellVertex.Update(
                updatedValue = expectedNewValue,
            )

            return object : ExpectedTestSubjectReaction.DeltaVerifier {
                override fun verifyExposedCorrectly() {
                    val exposedOngoingUpdate = assertNotNull(
                        actual = subjectProxy.observedCellVertex.ongoingUpdate,
                        message = "Expected an ongoing update on the subject cell vertex.",
                    )

                    assertEquals(
                        expected = expectedUpdate,
                        actual = exposedOngoingUpdate,
                        message = "Exposed ongoing update did not match the expected update.",
                    )
                }

                override fun verifyPropagatedCorrectly() {
                    val receivedUpdates = subjectProxy.getAndResetReceivedUpdates()

                    val lastReceivedUpdate = assertNotNull(
                        actual = receivedUpdates.lastOrNull(),
                        message = "Expected at least one update to be propagated.",
                    )

                    assertEquals(
                        expected = expectedUpdate,
                        actual = lastReceivedUpdate,
                        message = "Propagated update did not match the expected update.",
                    )
                }
            }
        }

        override fun prepareNewStateVerifier(
            propagationContext: Transactions.PropagationContext,
            subject: Cell<ValueT>,
        ): ExpectedTestSubjectReaction.NewStateVerifier = object : ExpectedTestSubjectReaction.NewStateVerifier {
            override fun verifyNewState(
                propagationContext: Transactions.PropagationContext,
            ) {
                val newValue = subject.vertex.getOldValue(
                    propagationContext = propagationContext,
                )

                assertEquals(
                    expected = expectedNewValue,
                    actual = newValue,
                    message = "Expected value to match the new value after update.",
                )
            }
        }
    }

    fun <ValueT> expectNoUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedCellReaction<ValueT> = object : ExpectedCellReaction<ValueT> {
        override fun prepareDeltaVerifier(
            propagationContext: Transactions.PropagationContext,
            subjectProxy: TestCellObserver<ValueT>,
        ): ExpectedTestSubjectReaction.DeltaVerifier {
            subjectProxy.resetReceivedUpdates()

            return object : ExpectedTestSubjectReaction.DeltaVerifier {
                override fun verifyExposedCorrectly() {
                    val ongoingUpdate = subjectProxy.observedCellVertex.ongoingUpdate

                    assertNull(
                        ongoingUpdate,
                        message = "Expected no ongoing update on the subject cell vertex.",
                    )
                }

                override fun verifyPropagatedCorrectly() {
                    when (intermediatePropagationTolerance) {
                        IntermediatePropagationTolerance.DoNotTolerate -> {
                            val receivedUpdates = subjectProxy.getAndResetReceivedUpdates()

                            assertEquals(
                                expected = emptyList(),
                                actual = receivedUpdates,
                                message = "Expected no updates to be propagated (intermediate propagation is not tolerated).",
                            )
                        }

                        IntermediatePropagationTolerance.Tolerate -> {
                            val lastReceivedUpdate = subjectProxy.getAndResetReceivedUpdates().lastOrNull()

                            assertNull(
                                actual = lastReceivedUpdate,
                                message = "Expected the last propagated update to be null (no update or update revocation).",
                            )
                        }
                    }
                }
            }
        }

        override fun prepareNewStateVerifier(
            propagationContext: Transactions.PropagationContext,
            subject: Cell<ValueT>,
        ): ExpectedTestSubjectReaction.NewStateVerifier {
            val originalValue = subject.vertex.getOldValue(
                propagationContext = propagationContext,
            )

            return object : ExpectedTestSubjectReaction.NewStateVerifier {
                override fun verifyNewState(
                    propagationContext: Transactions.PropagationContext,
                ) {
                    val newValue = subject.vertex.getOldValue(
                        propagationContext = propagationContext,
                    )

                    assertEquals(
                        expected = originalValue,
                        actual = newValue,
                        message = "Expected value to remain unchanged.",
                    )
                }
            }
        }
    }
}
