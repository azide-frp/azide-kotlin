package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.registerUpdateObserverOnline
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

typealias ExpectedCellReaction<ValueT> = ExpectedTestSubjectReaction<Cell<ValueT>>

typealias ExpectedCellState<ValueT> = ExpectedTestSubjectState<Cell<ValueT>>

typealias ExpectedCellTransition<ValueT> = ExpectedTestSubjectTransition<Cell<ValueT>>

private abstract class AbstractExpectedCellReaction<ValueT> : ExpectedCellReaction<ValueT> {
    final override fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<Cell<ValueT>>,
    ): ExpectedTestSubjectReaction.TestSubjectReactionVerifier =
        object : ExpectedTestSubjectReaction.TestSubjectReactionVerifier, UpdateObserver {
            private val subjectVertex: CellVertex<ValueT>
                get() = subjectLazy.value.vertex

            private var observerHandle: CellVertex.ObserverHandle? = null

            private var initialUpdate: CellVertex.Update<ValueT>? = null

            private val receivedUpdates = mutableListOf<CellVertex.Update<ValueT>?>()

            override fun install() {
                if (observerHandle != null) {
                    throw IllegalStateException("Cell verifier is already installed")
                }

                observerHandle = subjectVertex.registerUpdateObserverOnline(
                    propagationContext = propagationContext,
                    observer = this,
                )

                initialUpdate = subjectVertex.ongoingUpdate
            }

            override fun verifyReaction() {
                if (observerHandle == null) {
                    throw IllegalStateException("A non-installed verifier cannot be used for verification")
                }

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

            override fun uninstall() {
                val observerHandle =
                    this.observerHandle ?: throw IllegalStateException("Cannot uninstall a non-installed cell verifier")

                subjectVertex.unregisterObserver(
                    handle = observerHandle,
                )

                this.observerHandle = null
                this.initialUpdate = null
            }

            override fun handleUpdate(
                propagationContext: Transactions.PropagationContext,
            ) {
                receivedUpdates.add(subjectVertex.ongoingUpdate)
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
        expectedOldState = expectStableValue(
            expectedStableValue = expectedOldValue,
        ),
        expectedReaction = expectUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
            expectedUpdatedValue = expectedNewValue,
        ),
        expectedNewState = expectStableValue(
            expectedStableValue = expectedNewValue,
        ),
    )

    fun <ValueT> expectUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUpdatedValue: ValueT,
    ): ExpectedCellReaction<ValueT> = object : AbstractExpectedCellReaction<ValueT>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            intermediatePropagationTolerance

        override val expectedEffectiveUpdate: CellVertex.Update<ValueT> = CellVertex.Update(
            updatedValue = expectedUpdatedValue,
        )
    }

    fun <ValueT> expectStableValue(
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
        expectedOldState = expectStableValue(
            expectedStableValue = expectedUnaffectedValue,
        ),
        expectedReaction = expectNoUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
        ),
        expectedNewState = expectStableValue(
            expectedStableValue = expectedUnaffectedValue,
        ),
    )

    fun <ValueT> expectNoUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedCellReaction<ValueT> = object : AbstractExpectedCellReaction<ValueT>() {
        override val expectedEffectiveUpdate: CellVertex.Update<ValueT>? = null

        override val intermediatePropagationTolerance = intermediatePropagationTolerance
    }
}
