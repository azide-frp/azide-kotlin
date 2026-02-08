package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_spawn_rushedWrapUp_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils.SourceEventStreamTag
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_hold_spawn_rushedWrapUp_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceEventStreamEmits =
        EventStream_generic_testUtils.stimulationBank_sourceEventStreamEmits.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_spawn_rushedWrapUp() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_rushedWrapUp_testUtils.executeSpawnTransaction(
            subjectCellSpawnMoment = subjectMoment,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_spawn_rushedWrapUp_sourceEmitsSimultaneously() {
        slottedStimulationBank_sourceEventStreamEmits.forEach { slottedStimulationScenario ->
            test_spawn_rushedWrapUp_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_rushedWrapUp_sourceEmitsSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_rushedWrapUp_testUtils.executeSpawnTransaction(
            subjectCellSpawnMoment = subjectMoment,
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
}
