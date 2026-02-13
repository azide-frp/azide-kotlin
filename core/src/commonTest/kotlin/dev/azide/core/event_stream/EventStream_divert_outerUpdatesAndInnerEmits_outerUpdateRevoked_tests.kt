package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.event_stream.EventStream_divert_testUtils.SourceInnerEventStreamTag
import dev.azide.core.event_stream.EventStream_divert_testUtils.SourceOuterCellTag
import dev.azide.core.event_stream.EventStream_divert_testUtils.SuitableSlotCount
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.TestInputEventStreamTag
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_divert_outerUpdatesAndInnerEmits_outerUpdateRevoked_tests {
    private val slottedStimulationScenarioBank = TestStimulationScenarioBank.build(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceOuterCellTag,
        ),
        TestInputEventStreamTag.emissionScenario(
            inputEventStreamTag = SourceInnerEventStreamTag,
        ),
    ).distribute(
        slotCount = SuitableSlotCount,
    )

    @Test
    fun test_outerUpdatesAndOldInnerEmits_outerUpdateRevoked() {
        slottedStimulationScenarioBank.forEach {
            test_outerUpdatesAndOldInnerEmits_outerUpdateRevoked(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_outerUpdatesAndOldInnerEmits_outerUpdateRevoked(
        slottedStimulationScenario: TestSlottedStimulationScenario<SuitableSlotCount>,
    ) {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = TestStimulationMap.union(
                outerSourceCell.revokingUpdate(
                    tag = SourceOuterCellTag,
                    newValue = laterInnerSourceEventStream,
                ),
                earlierInnerSourceEventStream.emitting(
                    tag = SourceInnerEventStreamTag,
                    emittedEvent = 11,
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 11,
            ),
        )

        EventStream_divert_testUtils.verifyInnerEventStreamNotExposed(
            innerSourceEventStream = laterInnerSourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_outerUpdatesAndNewInnerEmits_outerUpdateRevoked() {
        slottedStimulationScenarioBank.forEach {
            test_outerUpdatesAndNewInnerEmits_outerUpdateRevoked(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_outerUpdatesAndNewInnerEmits_outerUpdateRevoked(
        slottedStimulationScenario: TestSlottedStimulationScenario<SuitableSlotCount>,
    ) {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = TestStimulationMap.union(
                outerSourceCell.revokingUpdate(
                    tag = SourceOuterCellTag,
                    newValue = laterInnerSourceEventStream,
                ),
                laterInnerSourceEventStream.emitting(
                    tag = SourceInnerEventStreamTag,
                    emittedEvent = 21,
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )

        EventStream_divert_testUtils.verifyInnerEventStreamNotExposed(
            innerSourceEventStream = laterInnerSourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }
}
