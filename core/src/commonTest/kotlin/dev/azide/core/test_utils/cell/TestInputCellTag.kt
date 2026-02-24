package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario_deprecated

interface TestInputCellTag {
    companion object {
        fun updateScenario(
            inputCellTag: TestInputCellTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputCellStimulationTag.Update(inputTag = inputCellTag),
        )

        fun revokedUpdateScenario(
            inputCellTag: TestInputCellTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputCellStimulationTag.Update(inputTag = inputCellTag),
            TestInputCellStimulationTag.UpdateRevocation(inputTag = inputCellTag),
        )

        fun correctedUpdateScenario(
            inputCellTag: TestInputCellTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputCellStimulationTag.Update(inputTag = inputCellTag),
            TestInputCellStimulationTag.UpdateCorrection(inputTag = inputCellTag),
        )
    }
}
