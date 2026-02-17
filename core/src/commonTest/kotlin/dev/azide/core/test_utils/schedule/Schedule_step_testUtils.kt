package dev.azide.core.test_utils.schedule

import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Schedule_step_testUtils {
    fun executeStepTransaction(
        inputStimulation: TestStimulation, expectedTargetImpact: ExpectedImpact,
    ) {
        Schedule_testUtils.executeTransactionWithImpactVerification(
            expectedTargetImpact = expectedTargetImpact,
        ) { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
