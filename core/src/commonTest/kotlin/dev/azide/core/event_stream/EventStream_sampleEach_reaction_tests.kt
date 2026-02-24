package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.sampleEach
import dev.azide.core.sampling
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.TestStimulation
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_sampleEach_reaction_tests {

    // region step source emits

    @Test
    fun test_step_sourceEmits() {
        val helperCell = TestInputCell(initialValue = 10)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        val inputPlan = generic_reaction_testUtils.InputStimulationPlan(
            unobservedInputStimulation = TestStimulation.Noop,
            observedInputStimulation = sourceEventStream.emit(helperCell.sampling),
        )

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
        )
    }

    // endregion

    // region step source emits revoked

    @Test
    fun test_step_sourceEmitsRevoked() {
        val helperCell = TestInputCell(initialValue = 10)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        val inputPlan = generic_reaction_testUtils.InputStimulationPlan(
            unobservedInputStimulation = TestStimulation.Noop,
            observedInputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(helperCell.sampling),
                sourceEventStream.revokeEmission(),
            ),
        )

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    // endregion

    // region step source emits corrected

    @Test
    fun test_step_sourceEmitsCorrected() {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        val inputPlan = generic_reaction_testUtils.InputStimulationPlan(
            unobservedInputStimulation = TestStimulation.Noop,
            observedInputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(helperCell1.sampling),
                sourceEventStream.correctEmission(helperCell2.sampling),
            ),
        )

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
        )
    }

    // endregion
}
