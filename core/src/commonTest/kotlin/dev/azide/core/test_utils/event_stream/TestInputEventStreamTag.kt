package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario_deprecated

interface TestInputEventStreamTag {
    companion object {
        fun emissionScenario(
            inputEventStreamTag: TestInputEventStreamTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputEventStreamStimulationTag.Emission(inputTag = inputEventStreamTag),
        )

        fun revokedEmissionScenario(
            inputEventStreamTag: TestInputEventStreamTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputEventStreamStimulationTag.Emission(inputTag = inputEventStreamTag),
            TestInputEventStreamStimulationTag.EmissionRevocation(inputTag = inputEventStreamTag),
        )

        fun correctedEmissionScenario(
            inputEventStreamTag: TestInputEventStreamTag,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated.of(
            TestInputEventStreamStimulationTag.Emission(inputTag = inputEventStreamTag),
            TestInputEventStreamStimulationTag.EmissionCorrection(inputTag = inputEventStreamTag),
        )
    }
}
