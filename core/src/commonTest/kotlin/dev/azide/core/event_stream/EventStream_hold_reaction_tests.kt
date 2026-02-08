package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.pullExternally
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils.SourceEventStreamTag
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_hold_reaction_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceEventStreamEmits =
        EventStream_generic_testUtils.stimulationBank_sourceEventStreamEmits.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceEventStreamEmitsRevoked =
        EventStream_generic_testUtils.stimulationBank_sourceEventStreamEmitsRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceEventStreamEmitsCorrected =
        EventStream_generic_testUtils.stimulationBank_sourceEventStreamEmitsCorrected.distribute(slotCount = SuitableSlotCount)
    @Test
    fun test_reaction_sourceUpdates() {
        slottedStimulationBank_sourceEventStreamEmits.forEach { slottedStimulationScenario ->
            test_reaction_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_reaction_sourceUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceEventStreamTag,
                emittedEvent = 10,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 0,
                expectedNewValue = 10,
            ),
        )
    }

    @Test
    fun test_reaction_sourceUpdatesRevoked() {
        slottedStimulationBank_sourceEventStreamEmitsRevoked.forEach { slottedStimulationScenario ->
            test_reaction_sourceUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_reaction_sourceUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceEventStreamTag,
                emittedEvent = 10,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_reaction_sourceUpdatesCorrected() {
        slottedStimulationBank_sourceEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_reaction_sourceUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_reaction_sourceUpdatesCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceEventStreamTag,
                intermediateEmittedEvent = 10,
                correctedEmittedEvent = 20,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 0,
                expectedNewValue = 20,
            ),
        )
    }
}
