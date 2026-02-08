package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object EventStream_generic_testUtils {
    data object SourceEventStreamTag : TestInputEventStreamTag

    val stimulationBank_sourceEventStreamEmits = TestStimulationBank.build(
        TestInputEventStreamTag.emissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )

    val stimulationBank_sourceEventStreamEmitsRevoked = TestStimulationBank.build(
        TestInputEventStreamTag.revokedEmissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )

    val stimulationBank_sourceEventStreamEmitsCorrected = TestStimulationBank.build(
        TestInputEventStreamTag.correctedEmissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )
}
