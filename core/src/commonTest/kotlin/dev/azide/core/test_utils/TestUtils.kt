package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.Moment
import dev.azide.core.MomentContext
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.internal.Transactions
import dev.azide.core.pullInternallyWrappedUp

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
        MomentContext.wrapUp(
            propagationContext = propagationContext,
        ) {
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

        moment.pullInternallyWrappedUp(
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

        val (result, _) = action.executeInternallyWrappedUp(
            propagationContext = propagationContext,
        )

        return@executeWithResult result
    }
}
