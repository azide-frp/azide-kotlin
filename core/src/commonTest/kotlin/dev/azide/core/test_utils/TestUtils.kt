package dev.azide.core.test_utils

import dev.azide.core.MomentContext
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

    fun <ResultT> pull(
        block: context(MomentContext) () -> ResultT,
    ): ResultT = Transactions.execute { propagationContext ->
        with(MomentContext.wrap(propagationContext)) {
            block()
        }
    }
}
