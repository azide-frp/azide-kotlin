package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.Moment
import dev.azide.core.MomentContext
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Transactions
import dev.azide.core.pullInternallyWrappedUp

internal object TestUtils {
    fun stimulateSeparately(
        inputStimulation: TestStimulation,
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
        inputStimulation: TestStimulation? = null,
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
        inputStimulation: TestStimulation? = null,
    ): ResultT = Transactions.executeWithResult { propagationContext ->
        inputStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        val (result, _) = action.executeInternallyWrappedUpUnpacked(
            propagationContext = propagationContext,
        )

        return@executeWithResult result
    }
}
