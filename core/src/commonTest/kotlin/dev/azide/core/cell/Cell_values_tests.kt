package dev.azide.core.cell

import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_spawn_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.values
import kotlin.test.Test

@Suppress("ClassName")
class Cell_values_tests {
    @Test
    fun test_spawn() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        EventStream_spawn_testUtils.testSpawn(
            subjectEventStreamSpawnMoment = sourceCell.values,
            slottedInputStimulation = null,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
        )
    }

    @Test
    fun test_spawn_sourceUpdatesSimultaneously() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        EventStream_spawn_testUtils.testSpawn(
            subjectEventStreamSpawnMoment = sourceCell.values,
            slottedInputStimulation = TestSlottedStimulation3(
                listOf(
                    sourceCell.update(newValue = 11),
                    TestStimulation.Noop,
                    TestStimulation.Noop,
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 11,
            ),
        )
    }

    @Test
    fun test_sourceUpdates() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = TestUtils.pullSeparately(
            sourceCell.values,
        )

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = sourceCell.update(newValue = 20),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 20,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = TestUtils.pullSeparately(
            sourceCell.values,
        )

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceCell.update(newValue = 20),
                    sourceCell.revokeUpdate(),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = TestUtils.pullSeparately(
            sourceCell.values,
        )

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceCell.update(newValue = 20),
                    sourceCell.correctUpdate(correctedNewValue = 21),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 21,
            ),
        )
    }
}
