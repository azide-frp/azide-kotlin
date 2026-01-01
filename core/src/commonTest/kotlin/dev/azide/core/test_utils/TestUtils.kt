package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.Moment
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

    fun <ResultT> pullSeparately(
        block: context(MomentContext) () -> ResultT,
    ): ResultT = Transactions.executeWithResult { propagationContext ->
        with(MomentContext.wrap(propagationContext)) {
            block()
        }
    }

    fun <ResultT> pullSeparately(
        moment: Moment<ResultT>,
        inputStimulation: TestInputStimulation? = null,
    ): ResultT = Transactions.executeWithResult { propagationContext ->
        inputStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        moment.pullInternally(
            propagationContext = propagationContext,
        )
    }

    fun <ResultT> executeSeparately(
        action: Action<ResultT>,
        inputStimulation: TestInputStimulation? = null,
    ): ResultT = Transactions.executeWithResult { propagationContext ->
        inputStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        val (result, _) = action.executeInternally(
            propagationContext = propagationContext,
        )

        return@executeWithResult result
    }
}
