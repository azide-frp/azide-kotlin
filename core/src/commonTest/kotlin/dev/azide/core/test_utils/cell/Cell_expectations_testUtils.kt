package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.test_utils.generic.AbstractExplicitExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.ExpectedTestSubjectState
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import kotlin.test.assertEquals

typealias ExpectedBasicCellUpdate<ValueT> = AbstractExplicitExpectedTestSubjectReaction<Cell<ValueT>, CellVertex.Update<ValueT>>

interface ExpectedCellValue<ValueT> : ExpectedTestSubjectState<Cell<ValueT>>

interface ExpectedCellValueTransition<ValueT> : ExpectedTestSubjectTransition<Cell<ValueT>, CellVertex.Update<ValueT>>

private abstract class AbstractExpectedCellUpdate<ValueT> : ExpectedBasicCellUpdate<ValueT>() {
    final override val expectedSubjectNotification: CellVertex.Update<ValueT>?
        get() = expectedEffectiveUpdate

    abstract val expectedEffectiveUpdate: CellVertex.Update<ValueT>?
}

abstract class AbstractExpectedCellValueTransition<ValueT> : ExpectedCellValueTransition<ValueT> {
    final override val expectedOldState: ExpectedCellValue<ValueT>
        get() = Cell_expectations_testUtils.expectStableValue(
            expectedValue = expectedOldValue,
        )

    final override val expectedNewState: ExpectedCellValue<ValueT>
        get() = Cell_expectations_testUtils.expectStableValue(
            expectedValue = expectedNewValue,
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

        override val expectedReaction: ExpectedBasicCellUpdate<ValueT> = expectUpdate(
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

        override val expectedReaction: ExpectedBasicCellUpdate<ValueT> = expectNoUpdate(
            intermediatePropagationTolerance = intermediatePropagationTolerance,
        )
    }

    fun <ValueT> expectStableValue(
        expectedValue: ValueT,
    ): ExpectedCellValue<ValueT> = object : ExpectedCellValue<ValueT> {
        override fun verifyStableState(
            propagationContext: Transactions.PropagationContext,
            subject: Cell<ValueT>,
        ) {
            val actualOldValue = subject.vertex.getOldValue(
                processingContext = propagationContext,
            )

            assertEquals(
                expected = expectedValue,
                actual = actualOldValue,
                message = "The stable value of the cell did not match the expected stable value.",
            )
        }
    }

    private fun <ValueT> expectUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
        expectedUpdatedValue: ValueT,
    ): ExpectedBasicCellUpdate<ValueT> = object : AbstractExpectedCellUpdate<ValueT>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            intermediatePropagationTolerance

        override val expectedEffectiveUpdate: CellVertex.Update<ValueT> = CellVertex.Update(
            updatedValue = expectedUpdatedValue,
        )
    }

    private fun <ValueT> expectNoUpdate(
        intermediatePropagationTolerance: IntermediatePropagationTolerance = IntermediatePropagationTolerance.DoNotTolerate,
    ): ExpectedBasicCellUpdate<ValueT> = object : AbstractExpectedCellUpdate<ValueT>() {
        override val expectedEffectiveUpdate: CellVertex.Update<ValueT>? = null

        override val intermediatePropagationTolerance = intermediatePropagationTolerance
    }
}
