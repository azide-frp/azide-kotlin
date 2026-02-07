package dev.azide.core.cell

import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.updatedValues
import kotlin.test.Test

@Suppress("ClassName")
class Cell_updatedValues_tests {
    @Test
    fun test_sourceUpdates() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_sourceUpdates(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_sourceUpdates(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.update(
                newValue = 20,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 20,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_sourceUpdates_revoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_sourceUpdates_revoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = 20,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_sourceUpdates_corrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_sourceUpdates_corrected(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = 20,
                correctedNewValue = 21,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 21,
            ),
        )
    }
}
