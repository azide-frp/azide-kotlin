package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

data class DoubleTestStimulation(
    val firstStimulation: TestInputStimulation,
    val secondStimulation: TestInputStimulation,
) {
    fun joint(): TestInputStimulation {
        return object : TestInputStimulation {
            override fun stimulate(
                propagationContext: Transactions.PropagationContext,
            ) {
                firstStimulation.stimulate(
                    propagationContext = propagationContext,
                )

                secondStimulation.stimulate(
                    propagationContext = propagationContext,
                )
            }

        }
    }
}
