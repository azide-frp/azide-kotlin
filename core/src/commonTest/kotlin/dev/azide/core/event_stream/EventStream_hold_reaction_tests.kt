package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.pullExternally
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.stateful.TestUtils_reaction
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_reaction_tests {
    @Test
    fun test_reaction_sourceUpdates() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_reaction_sourceUpdates(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_reaction_sourceUpdates(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        TestUtils_reaction.executeReactionTransaction(
            subject = subjectCell,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectTransition = Cell_expectations_testUtils.expectTransition(
                expectedOldValue = 0,
                expectedNewValue = 10,
            ),
        )
    }

    @Test
    fun test_reaction_sourceUpdatesRevoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_reaction_sourceUpdatesRevoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_reaction_sourceUpdatesRevoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        TestUtils_reaction.executeReactionTransaction(
            subject = subjectCell,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectTransition = Cell_expectations_testUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_reaction_sourceUpdatesCorrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_reaction_sourceUpdatesCorrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_reaction_sourceUpdatesCorrected(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        TestUtils_reaction.executeReactionTransaction(
            subject = subjectCell,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = 10,
                correctedEmittedEvent = 20,
            ).bind(dispatcher),
            expectedSubjectTransition = Cell_expectations_testUtils.expectTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 0,
                expectedNewValue = 20,
            ),
        )
    }
}
