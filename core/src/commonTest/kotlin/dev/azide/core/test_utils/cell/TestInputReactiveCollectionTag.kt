package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario_deprecated

interface TestInputReactiveCollectionTag {
    companion object {
        fun changeScenario(
            inputReactiveCollectionTag: TestInputReactiveCollectionTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputReactiveCollectionStimulationTag.Change(inputTag = inputReactiveCollectionTag),
        )

        fun revokedChangeScenario(
            inputReactiveCollectionTag: TestInputReactiveCollectionTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputReactiveCollectionStimulationTag.Change(inputTag = inputReactiveCollectionTag),
            TestInputReactiveCollectionStimulationTag.ChangeRevocation(inputTag = inputReactiveCollectionTag),
        )

        fun correctedChangeScenario(
            inputReactiveCollectionTag: TestInputReactiveCollectionTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputReactiveCollectionStimulationTag.Change(inputTag = inputReactiveCollectionTag),
            TestInputReactiveCollectionStimulationTag.ChangeCorrection(inputTag = inputReactiveCollectionTag),
        )
    }
}
