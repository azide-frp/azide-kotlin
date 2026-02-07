package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationTag

sealed interface TestInputEventStreamStimulationTag : TestStimulationTag {
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
