package dev.azide.core.event_stream

import dev.azide.core.map
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_map_tests {
    @Test
    fun test_sourceEmits() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.map { it.toString() }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = sourceEventStream.emit(
                    emittedEvent = 11,
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = "11",
            ),
        )
    }

    @Test
    fun test_sourceEmits_revoked() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.map { it.toString() }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(
                        emittedEvent = 11,
                    ),
                    sourceEventStream.revokeEmission(),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceEmits_corrected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.map { it.toString() }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(
                        emittedEvent = 11,
                    ),
                    sourceEventStream.correctEmission(
                        correctedEmittedEvent = 12,
                    ),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = "12",
            ),
        )
    }
}
