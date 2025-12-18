package dev.azide.core.test_utils

import dev.azide.core.internal.Transactions

internal object TestUtils {
    fun stimulateSeparately(
        inputStimulation: TestInputStimulation,
    ) {
        Transactions.execute { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
