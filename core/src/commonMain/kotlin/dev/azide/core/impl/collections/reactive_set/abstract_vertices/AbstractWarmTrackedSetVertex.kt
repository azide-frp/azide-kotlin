package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.AbstractLiveVertex
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.enqueueForCommitment

abstract class AbstractTrackedSetVertex<ElementT>() : AbstractLiveVertex(), TrackedSetVertex<ElementT>,
    CommittableVertex {
    private var _ongoingChange: SetChange<ElementT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingChange: SetChange<ElementT>?
        get() = _ongoingChange

    final override fun commit() {
        commit(
            ongoingChange = _ongoingChange,
        )

        _ongoingChange = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeChangeNotifyingListeners(
        propagationContext: PropagationContext,
        change: SetChange<ElementT>?,
    ) {
        exposeChange(
            propagationContext = propagationContext,
            change = change,
        )

        notifyListeners(
            propagationContext = propagationContext,
        )
    }

    protected fun exposeChange(
        propagationContext: PropagationContext,
        change: SetChange<ElementT>?,
    ) {
        _ongoingChange = change

        if (!_isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            _isEnqueuedForCommitment = true
        }
    }

    protected fun clearExposedChange() {
        _ongoingChange = null
    }

    protected open fun commit(
        ongoingChange: SetChange<ElementT>?,
    ) {
    }
}
