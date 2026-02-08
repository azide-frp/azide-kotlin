package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.TestInputEventStreamStimulationTag
import dev.azide.core.test_utils.event_stream.TestInputEventStreamTag
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind

@Suppress("ClassName")
object EventStream_divert_testUtils {
    typealias SuitableSlotCount = TestSlotCount.Count2

    typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    data object SourceOuterCellTag : TestInputCellTag

    data object SourceInnerEventStreamTag : TestInputEventStreamTag

    fun verifyInnerEventStreamNotExposed(
        innerSourceEventStream: TestInputEventStream<Int>,
        subjectEventStream: EventStream<Int>,
    ) {
        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = innerSourceEventStream.emitting(
                tag = SourceInnerEventStreamTag,
                emittedEvent = -1,
            ).bind(
                TestSlottedStimulationScenario.of(
                    slotCount = SuitableSlotCount,
                    slotStimulations = listOf(
                        TestStimulationScenario.Empty,
                        TestStimulationScenario.of(
                            TestInputEventStreamStimulationTag.Emission(
                                inputTag = SourceInnerEventStreamTag,
                            ),
                        ),
                    ),
                )
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )
    }

}
