package dev.azide.test_utils

import dev.azide.internal.Transactions

internal interface TestInputStimulation {
    companion object {
        fun combine(
            vararg stimulations: TestInputStimulation,
        ): TestInputStimulation = object : TestInputStimulation {
            override fun stimulate(
                propagationContext: Transactions.PropagationContext,
            ) {
                for (stimulation in stimulations) {
                    stimulation.stimulate(
                        propagationContext = propagationContext,
                    )
                }
            }
        }
    }

    fun stimulate(
        propagationContext: Transactions.PropagationContext,
    )
}
