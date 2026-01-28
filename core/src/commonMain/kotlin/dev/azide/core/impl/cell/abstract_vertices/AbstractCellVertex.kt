package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.AbstractLiveVertex
import dev.azide.core.impl.enqueueForCommitment

abstract class AbstractCellVertex<ValueT>() : AbstractLiveVertex(), CellVertex<ValueT>, CommittableVertex {
    private var _ongoingUpdate: CellVertex.Update<ValueT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingUpdate: CellVertex.Update<ValueT>?
        get() = _ongoingUpdate

    final override fun commit() {
        persist(
            ongoingUpdate = _ongoingUpdate,
        )

        transit()

        _ongoingUpdate = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeUpdateNotifyingListeners(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        exposeUpdate(
            propagationContext = propagationContext,
            update = update,
        )

        notifyListeners(
            propagationContext = propagationContext,
        )
    }

    protected fun exposeUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        _ongoingUpdate = update

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    protected fun clearExposedUpdate() {
        _ongoingUpdate = null
    }

    protected fun ensureEnqueuedForCommitment(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (!_isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            _isEnqueuedForCommitment = true
        }
    }

    protected open fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
    }

    protected open fun transit(
    ) {
    }
}
