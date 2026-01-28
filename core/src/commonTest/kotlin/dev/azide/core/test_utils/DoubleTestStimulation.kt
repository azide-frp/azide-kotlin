package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

data class DoubleTestStimulation(
    val firstStimulation: TestStimulation,
    val secondStimulation: TestStimulation,
) {
    fun joint(): TestStimulation {
        return object : TestStimulation {
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
