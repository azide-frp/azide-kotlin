package dev.azide.core.test_utils.schedule_generic

import dev.azide.core.Schedule
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Schedule_generic_start_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_testUtils.executeStartTransaction(
            subjectEffect = subjectSchedule,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
