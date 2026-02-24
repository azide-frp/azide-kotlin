package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationTag_deprecated

sealed interface TestInputEventStreamStimulationTag : TestStimulationTag_deprecated {
    data class Emission(
        val inputTag: TestInputEventStreamTag,
    ) : TestInputEventStreamStimulationTag

    data class EmissionRevocation(
        val inputTag: TestInputEventStreamTag,
    ) : TestInputEventStreamStimulationTag

    data class EmissionCorrection(
        val inputTag: TestInputEventStreamTag,
    ) : TestInputEventStreamStimulationTag
}
