package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x3
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_spawn_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_spawn_tests {
    @Test
    fun test_spawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsSimultaneously() {
        TestSlottedStimulationScenario1x3.entries.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario1x3,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 0,
                expectedNewValue = 10,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsRevokedSimultaneously() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsRevokedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = 10,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsCorrectedSimultaneously() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsCorrectedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.correctingEmission(
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
