package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation.Companion.combineInProvidedOrder

interface TestStimulation {
    data object Noop : TestStimulation {
        override fun stimulate(propagationContext: Transactions.PropagationContext) {
            // No operation performed
        }
    }

    companion object {
        fun executing(
            action: Action<*>,
        ): TestStimulation = object : TestStimulation {
            override fun stimulate(propagationContext: Transactions.PropagationContext) {
                action.executeInternallyWrappedUpUnpacked(
                    propagationContext = propagationContext,
                )
            }
        }

        /**
         * Combine the given [stimulations] into a single [TestStimulation] that executes them in the order they are
         * provided. This utility is meant for tests where the order of stimulation is part of the test's focus.
         */
        fun combineInProvidedOrder(
            stimulations: Iterable<TestStimulation>,
        ): TestStimulation = TestSequentialStimulation(
            consecutiveStimulations = stimulations.toList(),
        )

        /**
         * A thin wrapper around [combineInProvidedOrder] that allows for a more convenient vararg syntax when combining
         * multiple [TestStimulation] instances.
         */
        fun combineInProvidedOrder(
            vararg stimulations: TestStimulation,
        ): TestSequentialStimulation = TestSequentialStimulation(
            consecutiveStimulations = stimulations.toList(),
        )

        /**
         * Combine the given [stimulations] into a single [TestStimulation] that executes them in an arbitrary order.
         * This utility is meant for tests where the order of stimulation is not the focus of the test. A single
         * stimulation order will be picked automatically (not specified explicitly).
         */
        fun combineInArbitraryOrder(
            stimulations: Set<TestStimulation>,
        ): TestStimulation = combineInProvidedOrder(
            // Different implementations of `Set` may have different iteration orders, but any order will do
            stimulations = stimulations.toList(),
        )
    }

    fun stimulate(
        propagationContext: Transactions.PropagationContext,
    )
}
