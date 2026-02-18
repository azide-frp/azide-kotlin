package dev.azide.core.test_utils.schedule

import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact

import dev.azide.core.test_utils.generic.generic_testUtils
@Suppress("ClassName")
data object Schedule_step_testUtils {
    fun executeStepTransaction(
        inputStimulation: TestStimulation, expectedTargetImpact: ExpectedImpact,
    ) {
        generic_testUtils.executeTransactionWithImpactVerification(
            expectedTargetImpact = expectedTargetImpact,
        ) { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
