package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Transactions

interface TestStimulation {
    companion object {
        fun executing(
            action: Action<*>,
        ): TestStimulation = object : TestStimulation {
            override fun stimulate(propagationContext: Transactions.PropagationContext) {
                action.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )
            }
        }

        fun combine(
            stimulations: Iterable<TestStimulation>,
        ): TestStimulation = combine(*stimulations.toList().toTypedArray())

        fun combine(
            vararg stimulations: TestStimulation,
        ): TestStimulation = object : TestStimulation {
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
