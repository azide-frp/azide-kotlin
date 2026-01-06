package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.internal.Transactions

internal interface TestInputStimulation {
    companion object {
        fun executing(
            action: Action<*>,
        ): TestInputStimulation = object : TestInputStimulation {
            override fun stimulate(propagationContext: Transactions.PropagationContext) {
                action.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )
            }
        }

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
