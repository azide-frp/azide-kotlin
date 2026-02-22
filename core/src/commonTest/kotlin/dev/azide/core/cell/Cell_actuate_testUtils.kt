package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.effect_cell.Effect_Cell_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object Cell_actuate_testUtils {
    data object SourceEffectCellTag : TestInputCellTag

    val stimulationScenarioBank_sourceEffectCellUpdates = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceEffectCellTag,
        ),
    )

    val stimulationScenarioBank_sourceEffectCellUpdatesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceEffectCellTag,
        ),
    )

    val stimulationScenarioBank_sourceEffectCellUpdatesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceEffectCellTag,
        ),
    )

    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetEffect = TestTargetEffect.pure(result = -1)

        Effect_Cell_step_testUtils.testStep(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = preStimulationValue,
            ),
            expectedTargetImpact = targetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
    ) {
        val targetEffect = TestTargetEffect.pure(result = -1)

        generic_testUtils.executeTransactionWithImpactVerification(
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedTargetImpact = targetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetEffect = TestTargetEffect.pure(result = 0)

        Effect_Cell_step_testUtils.testStep(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = preStimulationValue,
                expectedNewValue = 0,
            ),
            expectedTargetImpact = targetEffect.expectIsStartedOnceButNotCancelled(),
        )
    }
}
