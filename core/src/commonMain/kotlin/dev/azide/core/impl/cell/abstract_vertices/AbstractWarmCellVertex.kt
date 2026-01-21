package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Listener
import dev.azide.core.impl.cell.WarmCellVertex
import dev.azide.core.impl.cell.WarmCellVertex.WarmListenerHandle
import dev.azide.core.impl.utils.weak_bag.MutableBag

abstract class AbstractWarmCellVertex<ValueT>() : WarmCellVertex<ValueT>, CommittableVertex {
    private val _registeredListeners: MutableBag<Listener> = MutableBag()

    private var _ongoingUpdate: CellVertex.Update<ValueT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingUpdate: CellVertex.Update<ValueT>?
        get() = _ongoingUpdate

    final override fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: Listener,
        mode: Vertex.ActivationMode,
    ): CellVertex.ListenerHandle {
        val internalHandle = _registeredListeners.add(listener)

        if (_registeredListeners.size == 1) {
            onFirstListenerRegistered(
                propagationContext = propagationContext,
                mode = mode,
            )
        }

        return WarmListenerHandle(
            internalHandle = internalHandle,
        )
    }

    final override fun unregisterListener(
        handle: CellVertex.ListenerHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? WarmListenerHandle ?: throw IllegalArgumentException("Invalid handle")

        _registeredListeners.remove(handleImpl.internalHandle)

        if (_registeredListeners.size == 0) {
            onLastListenerUnregistered()
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

    protected val hasListeners: Boolean
        get() = _registeredListeners.size > 0

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
        _registeredListeners.forEach { listener ->
            val listenerStatus = listener.handle(
                propagationContext = propagationContext,
            )

            // Remove the listener if it's unreachable
            listenerStatus == CellVertex.ListenerStatus.Unreachable
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

    protected open fun onFirstListenerRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ) {
    }

    protected open fun onLastListenerUnregistered() {
    }

    protected open fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
    }

    protected open fun transit(
    ) {
    }
}
