package dev.azide.core.test_utils

import dev.azide.core.internal.Transactions

internal interface TestInputStimulation {
    companion object {
        fun combine(
            vararg stimulations: dev.azide.core.test_utils.TestInputStimulation,
        ): dev.azide.core.test_utils.TestInputStimulation = object : dev.azide.core.test_utils.TestInputStimulation {
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
