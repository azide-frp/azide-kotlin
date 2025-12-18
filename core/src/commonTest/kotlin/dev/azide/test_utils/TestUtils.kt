package dev.azide.test_utils

import dev.azide.internal.Transactions

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
