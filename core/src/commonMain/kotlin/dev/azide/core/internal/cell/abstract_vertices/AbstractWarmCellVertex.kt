package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.CommittableVertex
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.CellVertex.Observer
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.cell.WarmCellVertex.WarmObserverHandle
import dev.azide.core.internal.utils.weak_bag.MutableBag

abstract class AbstractWarmCellVertex<ValueT>() : WarmCellVertex<ValueT>, CommittableVertex {
    private val _registeredObservers: MutableBag<Observer<ValueT>> = MutableBag()

    private var _ongoingUpdate: CellVertex.Update<ValueT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingUpdate: CellVertex.Update<ValueT>?
        get() = _ongoingUpdate

    final override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CellVertex.Observer<ValueT>,
    ): CellVertex.ObserverHandle {
        val internalHandle = _registeredObservers.add(observer)

        if (_registeredObservers.size == 1) {
            onFirstObserverRegistered(
                propagationContext = propagationContext,
            )
        }

        return WarmObserverHandle(
            internalHandle = internalHandle,
        )
    }

    final override fun unregisterObserver(
        handle: CellVertex.ObserverHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? WarmObserverHandle<ValueT> ?: throw IllegalArgumentException("Invalid handle")

        _registeredObservers.remove(handleImpl.internalHandle)

        if (_registeredObservers.size == 0) {
            onLastObserverUnregistered()
        }
    }

    final override fun commit() {
        persist(
            ongoingUpdate = _ongoingUpdate,
        )

        transit()

        _ongoingUpdate = null
        _isEnqueuedForCommitment = false
    }

    protected val hasObservers: Boolean
        get() = _registeredObservers.size > 0

    protected fun exposeAndPropagateUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        exposeUpdate(
            propagationContext = propagationContext,
            update = update,
        )

        propagateUpdate(
            propagationContext = propagationContext,
            update = update,
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

    private fun propagateUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        _registeredObservers.forEach { observer ->
            val observerStatus = observer.handleUpdateWithStatus(
                propagationContext = propagationContext,
                update = update,
            )

            // Remove the observer if it's unreachable
            observerStatus == CellVertex.ObserverStatus.Unreachable
        }
    }

    protected fun ensureEnqueuedForCommitment(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (!_isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            _isEnqueuedForCommitment = true
        }
    }

    protected open fun onFirstObserverRegistered(
        propagationContext: Transactions.PropagationContext,
    ) {
    }

    protected open fun onLastObserverUnregistered() {
    }

    protected open fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
    }

    protected open fun transit(
    ) {
    }
}
