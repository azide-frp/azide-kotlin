package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_divert_outerUpdatesAndInnerEmits_bothRevoked_tests {

    @Test
    fun test_outerUpdatesAndOldInnerEmits_bothRevoked_split() {
        // split: outer update unobserved, inner emission observed
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()
        val laterInnerSourceEventStream = TestInputEventStream<Int>()
        val outerSourceCell = TestInputCell(initialValue = earlierInnerSourceEventStream)
        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = outerSourceCell.revokingUpdate(newValue = laterInnerSourceEventStream).joint(),
                observedInputStimulation = earlierInnerSourceEventStream.revokingEmission(emittedEvent = 11).joint(),
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

    private fun test_outerUpdatesAndOldInnerEmits_bothRevoked_unobserved() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        // scenario where both stimulations are unobserved
        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.combineInProvidedOrder(
                    outerSourceCell.revokingUpdate(newValue = laterInnerSourceEventStream).joint(),
                    earlierInnerSourceEventStream.revokingEmission(emittedEvent = 11).joint(),
                ),
                observedInputStimulation = TestStimulation.Noop,
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

    @Test
    fun test_outerUpdatesAndNewInnerEmits_bothRevoked_unobservedAndSplit() {
        test_outerUpdatesAndNewInnerEmits_bothRevoked_unobserved()
    }

    @Test
    fun test_outerUpdatesAndNewInnerEmits_bothRevoked_split() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()
        val laterInnerSourceEventStream = TestInputEventStream<Int>()
        val outerSourceCell = TestInputCell(initialValue = earlierInnerSourceEventStream)
        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = outerSourceCell.revokingUpdate(newValue = laterInnerSourceEventStream).joint(),
                observedInputStimulation = laterInnerSourceEventStream.revokingEmission(emittedEvent = 21).joint(),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )

        EventStream_divert_testUtils.verifyInnerEventStreamNotExposed(
            innerSourceEventStream = laterInnerSourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    private fun test_outerUpdatesAndNewInnerEmits_bothRevoked_unobserved() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.combineInProvidedOrder(
                    outerSourceCell.revokingUpdate(newValue = laterInnerSourceEventStream).joint(),
                    laterInnerSourceEventStream.revokingEmission(emittedEvent = 21).joint(),
                ),
                observedInputStimulation = TestStimulation.Noop,
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )

        EventStream_divert_testUtils.verifyInnerEventStreamNotExposed(
            innerSourceEventStream = laterInnerSourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }
}
