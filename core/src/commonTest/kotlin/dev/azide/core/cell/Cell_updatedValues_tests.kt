package dev.azide.core.cell

import dev.azide.core.test_utils.TestSlottedStimulationScenario1x2
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.updatedValues
import kotlin.test.Test

@Suppress("ClassName")
class Cell_updatedValues_tests {
    @Test
    fun test_sourceUpdates() {
        TestSlottedStimulationScenario1x2.entries.forEach { slottedStimulationScenario ->
            test_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates(
        slottedStimulationScenario: TestSlottedStimulationScenario1x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.update(
                newValue = 20,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 20,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_sourceUpdates_revoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates_revoked(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = 20,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_sourceUpdates_corrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates_corrected(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
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
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 21,
            ),
        )
    }
}
