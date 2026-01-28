package dev.azide.core.test_utils.schedule_generic

import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Schedule_generic_cancelledRevoked_testUtils {
    fun executeCancelTransaction(
        subjectOutcome: Effect.Outcome<Unit>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
