package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

interface ExpectedCellUpdate<ValueT> : ExpectedTestSubjectReaction<Cell<ValueT>>

interface ExpectedCellValue<ValueT> : ExpectedTestSubjectState<Cell<ValueT>>

interface ExpectedCellValueTransition<ValueT> : ExpectedTestSubjectTransition<Cell<ValueT>>

private abstract class AbstractExpectedCellUpdate<ValueT> : ExpectedCellUpdate<ValueT> {
    final override fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<Cell<ValueT>>,
    ): ExpectedTestSubjectReaction.TestSubjectReactionVerifier =
        object : ExpectedTestSubjectReaction.TestSubjectReactionVerifier, BoundListener {
            private val subjectVertex: CellVertex<ValueT>
                get() = subjectLazy.value.vertex

            private var listenerHandle: ListenerHandle? = null

            private var initialUpdate: CellVertex.Update<ValueT>? = null

            private val receivedUpdates = mutableListOf<CellVertex.Update<ValueT>?>()

            override fun install() {
                if (listenerHandle != null) {
                    throw IllegalStateException("Cell verifier is already installed")
                }

                listenerHandle = subjectVertex.registerBoundListenerOnline(
                    propagationContext = propagationContext,
                    listener = this,
                )

                initialUpdate = subjectVertex.ongoingUpdate
            }

            override fun verifyReaction() {
                if (listenerHandle == null) {
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
                val listenerHandle =
                    this.listenerHandle ?: throw IllegalStateException("Cannot uninstall a non-installed cell verifier")

                subjectVertex.unregisterListener(
                    handle = listenerHandle,
                )

                this.listenerHandle = null
                this.initialUpdate = null
            }

            override fun handle(
                propagationContext: Transactions.PropagationContext,
            ) {
                receivedUpdates.add(subjectVertex.ongoingUpdate)
            }
        }

    abstract val intermediatePropagationTolerance: IntermediatePropagationTolerance

    abstract val expectedEffectiveUpdate: CellVertex.Update<ValueT>?
}

abstract class AbstractExpectedCellValueTransition<ValueT> : ExpectedCellValueTransition<ValueT> {
    final override val expectedOldState: ExpectedCellValue<ValueT>
        get() = Cell_expectations_testUtils.expectStableValue(
            expectedStableValue = expectedOldValue,
        )

    final override val expectedNewState: ExpectedCellValue<ValueT>
        get() = Cell_expectations_testUtils.expectStableValue(
            expectedStableValue = expectedNewValue,
        )

    abstract val expectedOldValue: ValueT

    abstract val expectedNewValue: ValueT
}

@Suppress("ClassName")
object Cell_expectations_testUtils {
    fun <ValueT> expectValueTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedOldValue: ValueT,
        expectedNewValue: ValueT,
    ): ExpectedCellValueTransition<ValueT> = object : AbstractExpectedCellValueTransition<ValueT>() {
        override val expectedOldValue: ValueT = expectedOldValue

        override val expectedNewValue: ValueT = expectedNewValue

        override val expectedReaction: ExpectedCellUpdate<ValueT> = expectUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
            expectedUpdatedValue = expectedNewValue,
        )
    }

    fun <ValueT> expectNoValueTransition(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUnaffectedValue: ValueT,
    ): ExpectedCellValueTransition<ValueT> = object : AbstractExpectedCellValueTransition<ValueT>() {
        override val expectedOldValue: ValueT = expectedUnaffectedValue

        override val expectedNewValue: ValueT = expectedUnaffectedValue

        override val expectedReaction: ExpectedCellUpdate<ValueT> = expectNoUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
        )
    }

    fun <ValueT> expectStableValue(
        expectedStableValue: ValueT,
    ): ExpectedCellValue<ValueT> = object : ExpectedCellValue<ValueT> {
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

    private fun <ValueT> expectUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUpdatedValue: ValueT,
    ): ExpectedCellUpdate<ValueT> = object : AbstractExpectedCellUpdate<ValueT>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            intermediatePropagationTolerance

        override val expectedEffectiveUpdate: CellVertex.Update<ValueT> = CellVertex.Update(
            updatedValue = expectedUpdatedValue,
        )
    }

    private fun <ValueT> expectNoUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedCellUpdate<ValueT> = object : AbstractExpectedCellUpdate<ValueT>() {
        override val expectedEffectiveUpdate: CellVertex.Update<ValueT>? = null

        override val intermediatePropagationTolerance = intermediatePropagationTolerance
    }
}
