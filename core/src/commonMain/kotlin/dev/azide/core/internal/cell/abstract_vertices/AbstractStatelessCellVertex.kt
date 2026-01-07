package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex.ActivationMode
import dev.azide.core.internal.cell.CellVertex

abstract class AbstractStatelessCellVertex<ValueT> : AbstractWarmCellVertex<ValueT>() {
    final override fun onFirstObserverRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: ActivationMode,
    ) {
        when (mode) {
            ActivationMode.Online -> {
                val updateOnActivation = activateOnline(
                    propagationContext = propagationContext,
                )

                exposeUpdate(
                    propagationContext = propagationContext,
                    update = updateOnActivation,
                )
            }

            ActivationMode.Offline -> {
                activateOffline(
                    propagationContext = propagationContext,
                )
            }
        }
    }

    final override fun onLastObserverUnregistered() {
        deactivate()

        clearExposedUpdate()
    }

    abstract fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>?

    abstract fun activateOffline(
        propagationContext: Transactions.PropagationContext,
    )

    abstract fun deactivate()
}
