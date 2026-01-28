package dev.azide.core.test_utils.schedule_generic

import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Schedule_generic_step_testUtils {
    fun executeStepTransaction(
        inputStimulation: TestStimulation,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_step_testUtils.executeStepTransaction(
            subject = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            inputStimulation = inputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
