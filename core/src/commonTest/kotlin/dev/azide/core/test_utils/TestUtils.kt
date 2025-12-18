package dev.azide.core.test_utils

import dev.azide.core.internal.Transactions

internal object TestUtils {
    fun stimulateSeparately(
        inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
    ) {
        Transactions.execute { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
