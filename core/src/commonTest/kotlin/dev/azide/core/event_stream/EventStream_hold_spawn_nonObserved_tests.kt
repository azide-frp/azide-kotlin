package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_spawn_nonPerceived_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils.SourceEventStreamTag
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_hold_spawn_nonObserved_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceEventStreamEmits =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmits.distribute(slotCount = SuitableSlotCount)
    @Test
    fun test_spawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val expectedUnaffectedState = Cell_expectations_testUtils.expectStableValue(
            expectedValue = 0,
        )

        Cell_spawn_nonPerceived_testUtils.testSpawn(
            subjectCellSpawnMoment = subjectMoment,
            expectedOldSubjectValue = expectedUnaffectedState,
            expectedNewSubjectValue = expectedUnaffectedState,
        )
    }

    @Test
    fun test_spawn_sourceEmitsSimultaneously() {
        slottedStimulationScenarioBank_sourceEventStreamEmits.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_nonPerceived_testUtils.testSpawn(
            subjectCellSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceEventStreamTag,
                emittedEvent = 10,
            ).bind(slottedStimulationScenario),
            expectedOldSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 0,
            ),
            expectedNewSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 10,
            ),
        )
    }
}
