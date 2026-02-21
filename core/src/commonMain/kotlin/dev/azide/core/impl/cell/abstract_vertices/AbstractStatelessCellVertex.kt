package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex

abstract class AbstractStatelessCellVertex<ValueT> : AbstractCellVertex<ValueT>() {
    final override fun onFirstListenerRegistered(
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

    final override fun onLastListenerUnregistered() {
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
