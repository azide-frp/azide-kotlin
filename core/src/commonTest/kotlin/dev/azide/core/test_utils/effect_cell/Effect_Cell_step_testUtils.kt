package dev.azide.core.test_utils.effect_cell

import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_Cell_step_testUtils {
    fun <SubjectT> executeStepTransaction(
        subjectCell: SubjectT,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulation: TestStimulation,
        expectedSubjectValueTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_step_testUtils.executeStepTransaction(
            subject = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = inputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
