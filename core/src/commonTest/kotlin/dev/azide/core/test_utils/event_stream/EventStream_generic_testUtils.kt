package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object EventStream_generic_testUtils {
    data object SourceEventStreamTag : TestInputEventStreamTag

    val stimulationScenarioBank_sourceEventStreamEmits = TestStimulationScenarioBank.build(
        TestInputEventStreamTag.emissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )

    val stimulationScenarioBank_sourceEventStreamEmitsRevoked = TestStimulationScenarioBank.build(
        TestInputEventStreamTag.revokedEmissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )

    val stimulationScenarioBank_sourceEventStreamEmitsCorrected = TestStimulationScenarioBank.build(
        TestInputEventStreamTag.correctedEmissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )
}
