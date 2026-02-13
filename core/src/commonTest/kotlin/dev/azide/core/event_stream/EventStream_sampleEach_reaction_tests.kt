package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.sampleEach
import dev.azide.core.sampling
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils.SourceEventStreamTag
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_sampleEach_reaction_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceEventStreamEmits =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmits.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceEventStreamEmitsRevoked =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmitsRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmitsCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_step_sourceEmits() {
        slottedStimulationScenarioBank_sourceEventStreamEmits.forEach { slottedStimulationScenario ->
            test_step_sourceEmits(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceEmits(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell = TestInputCell(initialValue = 10)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceEventStreamTag,
                emittedEvent = helperCell.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
        )
    }

    @Test
    fun test_step_sourceEmitsRevoked() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsRevoked.forEach { slottedStimulationScenario ->
            test_step_sourceEmitsRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceEmitsRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell = TestInputCell(initialValue = 10)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceEventStreamTag,
                emittedEvent = helperCell.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_step_sourceEmitsCorrected() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_step_sourceEmitsCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceEmitsCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceEventStreamTag,
                intermediateEmittedEvent = helperCell1.sampling,
                correctedEmittedEvent = helperCell2.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
        )
    }
}
