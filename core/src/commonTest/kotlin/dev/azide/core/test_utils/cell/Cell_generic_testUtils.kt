package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object Cell_generic_testUtils {
    data object SourceCellTag : TestInputCellTag

    val stimulationScenarioBank_sourceCellUpdates = TestStimulationScenarioBank.build(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationScenarioBank_sourceCellUpdatesRevoked = TestStimulationScenarioBank.build(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationScenarioBank_sourceCellUpdatesCorrected = TestStimulationScenarioBank.build(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )
}
