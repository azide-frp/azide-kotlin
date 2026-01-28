package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario

interface TestInputCellTag {
    companion object {
        fun updateScenario(
            inputCellTag: TestInputCellTag,
        ): TestStimulationScenario = TestStimulationScenario.of(
            TestInputCellStimulationTag.Update(inputTag = inputCellTag),
        )

        fun revokedUpdateScenario(
            inputCellTag: TestInputCellTag,
        ): TestStimulationScenario = TestStimulationScenario.of(
            TestInputCellStimulationTag.Update(inputTag = inputCellTag),
            TestInputCellStimulationTag.UpdateRevocation(inputTag = inputCellTag),
        )

        fun correctedUpdateScenario(
            inputCellTag: TestInputCellTag,
        ): TestStimulationScenario = TestStimulationScenario.of(
            TestInputCellStimulationTag.Update(inputTag = inputCellTag),
            TestInputCellStimulationTag.UpdateCorrection(inputTag = inputCellTag),
        )
    }
}
