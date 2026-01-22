package dev.azide.core.test_utils.schedules

import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.effects.EffectTestUtils_step
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object ScheduleTestUtils_step {
    fun executeStepTransaction(
        inputStimulation: TestInputStimulation,
        expectedTargetImpact: ExpectedImpact,
    ) {
        EffectTestUtils_step.executeStepTransaction(
            subject = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            inputStimulation = inputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition(
                expectedOldState = ExpectedTestSubjectState.None,
                expectedNewState = ExpectedTestSubjectState.None,
                expectedReaction = ExpectedTestSubjectReaction.None,
            ),
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
