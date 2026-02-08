package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.TestInputEventStreamTag
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_divert_outerUpdatesAndInnerEmits_outerUpdateRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2
    
    private data object SourceOuterCellTag : TestInputCellTag

    private data object SourceInnerEventStreamTag : TestInputEventStreamTag

    private val slottedStimulationBank = TestStimulationBank.build(
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
        slottedStimulationBank.forEach {
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
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = TestStimulationMap.union(
                    outerSourceCell.revokingUpdate(
                        tag = SourceOuterCellTag,
                        newValue = laterInnerSourceEventStream,
                    ),
                    earlierInnerSourceEventStream.emitting(
                        tag = SourceInnerEventStreamTag,
                        emittedEvent = 11,
                    ),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 11,
            ),
        )

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 22,
            ).bind(TestSlotDispatcher1x2.Case1),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )
    }

    @Test
    fun test_outerUpdatesAndNewInnerEmits_outerUpdateRevoked() {
        slottedStimulationBank.forEach {
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
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = TestStimulationMap.union(
                    outerSourceCell.revokingUpdate(
                        tag = SourceOuterCellTag,
                        newValue = laterInnerSourceEventStream,
                    ),
                    laterInnerSourceEventStream.emitting(
                        tag = SourceInnerEventStreamTag,
                        emittedEvent = 21,
                    ),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 22,
            ).bind(TestSlotDispatcher1x2.Case1),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )
    }
}
