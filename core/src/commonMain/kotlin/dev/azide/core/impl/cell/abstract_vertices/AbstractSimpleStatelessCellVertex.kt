package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex

abstract class AbstractSimpleStatelessCellVertex<ValueT> : AbstractStatelessCellVertex<ValueT>() {
    /**
     * Activate the vertex online.
     */
    final override fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>? = activate(
        propagationContext = propagationContext,
        mode = ActivationMode.Online,
    )

    /**
     * Activate the vertex offline.
     */
    final override fun activateOffline(
        propagationContext: Transactions.PropagationContext,
    ) {
        val computedUpdate = activate(
            propagationContext = propagationContext,
            mode = ActivationMode.Offline,
        )

        if (computedUpdate != null) {
            throw AssertionError("The vertex unexpectedly computed the update in the offline activation mode")
        }
    }

    /**
     * Activate the vertex abstracting over the activation mode. In the online mode, it may return a computed update;
     * in the offline mode, it must return null.
     */
    abstract fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: ActivationMode,
    ): CellVertex.Update<ValueT>?
}
