package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank_deprecated

@Suppress("ClassName")
data object Cell_generic_testUtils {
    data object SourceCellTag : TestInputCellTag

    val stimulationScenarioBank_sourceCellUpdates = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationScenarioBank_sourceCellUpdatesRevoked = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationScenarioBank_sourceCellUpdatesCorrected = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )
}
