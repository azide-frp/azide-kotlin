package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationTag_deprecated

sealed interface TestInputCellStimulationTag : TestStimulationTag_deprecated {
    data class Update(
        val inputTag: TestInputCellTag,
    ) : TestInputCellStimulationTag

    data class UpdateRevocation(
        val inputTag: TestInputCellTag,
    ) : TestInputCellStimulationTag

    data class UpdateCorrection(
        val inputTag: TestInputCellTag,
    ) : TestInputCellStimulationTag
}
