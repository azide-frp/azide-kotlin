package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x4
import dev.azide.core.test_utils.TestSlotDispatcher2x4
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effects.EffectTestUtils_startRevoked_quickCancelled
import dev.azide.core.test_utils.expectIsNotStarted
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_startRevoked_quickCancelled_tests {
    @Test
    fun test_startRevoked_quickCancelled() {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        EffectTestUtils_startRevoked_quickCancelled.executeStartTransaction(
            subjectEffect = subjectEffect,
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled_sourceUpdatesSimultaneously() {
        TestSlotDispatcher1x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceUpdatesSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceUpdatesSimultaneously(
        dispatcher: TestSlotDispatcher1x4,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        EffectTestUtils_startRevoked_quickCancelled.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled_sourceUpdatesRevokedSimultaneously() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceUpdatesRevokedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceUpdatesRevokedSimultaneously(
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        EffectTestUtils_startRevoked_quickCancelled.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled_sourceUpdatesCorrectedSimultaneously() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceUpdatesCorrectedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceUpdatesCorrectedSimultaneously(
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        EffectTestUtils_startRevoked_quickCancelled.executeStartTransaction(
            subjectEffect = subjectEffect,
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

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
        )
    }
}
