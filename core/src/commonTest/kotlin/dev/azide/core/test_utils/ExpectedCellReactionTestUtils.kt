package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.WarmCellVertex
import dev.azide.core.impl.cell.registerObserverOnline
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

typealias ExpectedCellReaction<ValueT> = ExpectedTestSubjectReaction<Cell<ValueT>>

typealias ExpectedCellState<ValueT> = ExpectedTestSubjectState<Cell<ValueT>>

typealias ExpectedCellTransition<ValueT> = ExpectedTestSubjectTransition<Cell<ValueT>>

private abstract class AbstractExpectedCellReaction<ValueT> : ExpectedCellReaction<ValueT> {
    final override fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subject: Cell<ValueT>,
    ): ExpectedTestSubjectReaction.TestSubjectReactionVerifier {
        val subjectVertex = subject.vertex

        return object : ExpectedTestSubjectReaction.TestSubjectReactionVerifier, WarmCellVertex.BasicObserver<ValueT> {
            private val observerHandle = subjectVertex.registerObserverOnline(
                propagationContext = propagationContext,
                observer = this,
            )

            private val initialUpdate: CellVertex.Update<ValueT>? = subjectVertex.ongoingUpdate

            private val receivedUpdates = mutableListOf<CellVertex.Update<ValueT>?>()

            override fun verifyReaction() {
                assertEquals(
                    expected = expectedEffectiveUpdate,
                    actual = subjectVertex.ongoingUpdate,
                    message = "Exposed ongoing update did not match the expected update.",
                )

                val effectiveUpdate = when {
                    receivedUpdates.isNotEmpty() -> receivedUpdates.last()
                    else -> initialUpdate
                }

                assertEquals(
                    expected = expectedEffectiveUpdate,
                    actual = effectiveUpdate,
                    message = "The effective received update did not match the expected update.",
                )

                subjectVertex.unregisterObserver(
                    handle = observerHandle,
                )

                when (intermediatePropagationTolerance) {
                    IntermediatePropagationTolerance.DoNotTolerate -> {
                        assertTrue(
                            actual = receivedUpdates.size <= 1,
                            message = "Expected at most one update to be propagated, but received ${receivedUpdates.size} updates (intermediate propagation is not tolerated).",
                        )
                    }

                    IntermediatePropagationTolerance.Tolerate -> {}
                }
            }

            override fun handleUpdate(
                propagationContext: Transactions.PropagationContext,
                update: CellVertex.Update<ValueT>?,
            ) {
                receivedUpdates.add(update)
            }
        }
    }

    abstract val intermediatePropagationTolerance: IntermediatePropagationTolerance

    abstract val expectedEffectiveUpdate: CellVertex.Update<ValueT>?
}

object ExpectedCellReactionTestUtils {
    fun <ValueT> expectTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldValue: ValueT,
        expectedNewValue: ValueT,
    ): ExpectedCellTransition<ValueT> = ExpectedCellTransition(
        expectedOldState = _expectStableValue(
            expectedStableValue = expectedOldValue,
        ),
        expectedReaction = _expectUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
            expectedUpdatedValue = expectedNewValue,
        ),
        expectedNewState = _expectStableValue(
            expectedStableValue = expectedNewValue,
        ),
    )

    fun <ValueT> _expectUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUpdatedValue: ValueT,
    ): ExpectedCellReaction<ValueT> = object : AbstractExpectedCellReaction<ValueT>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            intermediatePropagationTolerance

        override val expectedEffectiveUpdate: CellVertex.Update<ValueT> = CellVertex.Update(
            updatedValue = expectedUpdatedValue,
        )
    }

    fun <ValueT> _expectStableValue(
        expectedStableValue: ValueT,
    ): ExpectedCellState<ValueT> = object : ExpectedCellState<ValueT> {
        override fun verifyStableState(
            propagationContext: Transactions.PropagationContext,
            subject: Cell<ValueT>,
        ) {
            assertEquals(
                expected = expectedStableValue,
                actual = subject.vertex.getOldValue(
                    propagationContext = propagationContext,
                ),
                message = "The stable value of the cell did not match the expected stable value.",
            )
        }
    }

    fun <ValueT> expectNoTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedValue: ValueT,
    ): ExpectedCellTransition<ValueT> = ExpectedCellTransition(
        expectedOldState = _expectStableValue(
            expectedStableValue = expectedUnaffectedValue,
        ),
        expectedReaction = _expectNoUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
        ),
        expectedNewState = _expectStableValue(
            expectedStableValue = expectedUnaffectedValue,
        ),
    )

    fun <ValueT> _expectNoUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedCellReaction<ValueT> = object : AbstractExpectedCellReaction<ValueT>() {
        override val expectedEffectiveUpdate: CellVertex.Update<ValueT>? = null

        override val intermediatePropagationTolerance = intermediatePropagationTolerance
    }
}
