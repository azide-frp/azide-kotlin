package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object Cell_generic_testUtils {
    data object SourceCellTag : TestInputCellTag

    val stimulationBank_sourceCellUpdates = TestStimulationBank.build(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationBank_sourceCellUpdatesRevoked = TestStimulationBank.build(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )

    val stimulationBank_sourceCellUpdatesCorrected = TestStimulationBank.build(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceCellTag,
        ),
    )
}
