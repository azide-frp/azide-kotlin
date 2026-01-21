package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.WarmCellVertex
import dev.azide.core.impl.cell.WarmCellVertex.WarmObserverHandle
import dev.azide.core.impl.utils.weak_bag.MutableBag

abstract class AbstractWarmCellVertex<ValueT>() : WarmCellVertex<ValueT>, CommittableVertex {
    private val _registeredObservers: MutableBag<UpdateObserver> = MutableBag()

    private var _ongoingUpdate: CellVertex.Update<ValueT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingUpdate: CellVertex.Update<ValueT>?
        get() = _ongoingUpdate

    final override fun registerUpdateObserver(
        propagationContext: Transactions.PropagationContext,
        observer: UpdateObserver,
        mode: Vertex.ActivationMode,
    ): CellVertex.ObserverHandle {
        val internalHandle = _registeredObservers.add(observer)

        if (_registeredObservers.size == 1) {
            onFirstObserverRegistered(
                propagationContext = propagationContext,
                mode = mode,
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
            handle as? WarmObserverHandle ?: throw IllegalArgumentException("Invalid handle")

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

        propagateUpdateNotification(
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

    private fun propagateUpdateNotification(
        propagationContext: Transactions.PropagationContext,
    ) {
        _registeredObservers.forEach { observer ->
            val observerStatus = observer.handleUpdate(
                propagationContext = propagationContext,
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
        mode: Vertex.ActivationMode,
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
