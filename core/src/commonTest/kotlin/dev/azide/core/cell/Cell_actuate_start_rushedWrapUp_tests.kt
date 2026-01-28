package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.effect_generic.Effect_Cell_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_start_rushedWrapUp_tests {
    @Test
    fun test_start() {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1.expectIsStartedOnceButNotCancelled(),
        )
    }

    @Test
    fun test_start_sourceUpdatesSimultaneously() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_start_sourceUpdatesSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_start_sourceUpdatesSimultaneously(
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
            ),
        )
    }
}
