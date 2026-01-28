package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_EventStream_step_testUtils {
    fun <SubjectT> executeStepTransaction(
        subjectEventStream: SubjectT,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulation: TestStimulation,
        expectedSubjectEmission: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_step_testUtils.executeStepTransaction(
            subject = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = inputStimulation,
            expectedSubjectTransition = expectedSubjectEmission,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
