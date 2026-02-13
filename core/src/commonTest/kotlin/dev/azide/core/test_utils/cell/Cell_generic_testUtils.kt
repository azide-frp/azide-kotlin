package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object Cell_generic_testUtils {
    data object SourceCellTag : TestInputCellTag

    val stimulationScenarioBank_sourceCellUpdates = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationScenarioBank_sourceCellUpdatesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationScenarioBank_sourceCellUpdatesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )
}
