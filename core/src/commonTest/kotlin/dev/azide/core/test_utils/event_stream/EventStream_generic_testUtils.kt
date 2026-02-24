package dev.azide.core.test_utils.event_stream

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank_deprecated

@Suppress("ClassName")
data object EventStream_generic_testUtils {
    data object SourceEventStreamTag : TestInputEventStreamTag

    val stimulationScenarioBank_sourceEventStreamEmits = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputEventStreamTag.emissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )

    val stimulationScenarioBank_sourceEventStreamEmitsRevoked = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputEventStreamTag.revokedEmissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )

    val stimulationScenarioBank_sourceEventStreamEmitsCorrected = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputEventStreamTag.correctedEmissionScenario(
            inputEventStreamTag = SourceEventStreamTag,
        ),
    )
}
