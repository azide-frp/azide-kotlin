package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario

interface TestInputReactiveCollectionTag {
    companion object {
        fun changeScenario(
            inputReactiveCollectionTag: TestInputReactiveCollectionTag,
        ): TestStimulationScenario = TestStimulationScenario.of(
            TestInputReactiveCollectionStimulationTag.Change(inputTag = inputReactiveCollectionTag),
        )

        fun revokedChangeScenario(
            inputReactiveCollectionTag: TestInputReactiveCollectionTag,
        ): TestStimulationScenario = TestStimulationScenario.of(
            TestInputReactiveCollectionStimulationTag.Change(inputTag = inputReactiveCollectionTag),
            TestInputReactiveCollectionStimulationTag.ChangeRevocation(inputTag = inputReactiveCollectionTag),
        )

        fun correctedChangeScenario(
            inputReactiveCollectionTag: TestInputReactiveCollectionTag,
        ): TestStimulationScenario = TestStimulationScenario.of(
            TestInputReactiveCollectionStimulationTag.Change(inputTag = inputReactiveCollectionTag),
            TestInputReactiveCollectionStimulationTag.ChangeCorrection(inputTag = inputReactiveCollectionTag),
        )
    }
}
