package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestSlotDispatcher2x3
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effect_cell.Effect_Cell_startRevoked_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.generic.ExpectedImpact
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_startRevoked_tests {
    @Test
    fun test_startRevoked() {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesSimultaneously() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_startRevoked_sourceUpdatesSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesSimultaneously(
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesRevokedSimultaneously() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_startRevoked_sourceUpdatesRevokedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesRevokedSimultaneously(
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesCorrectedSimultaneously() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_startRevoked_sourceUpdatesCorrectedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesCorrectedSimultaneously(
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
            ),
        )
    }
}
