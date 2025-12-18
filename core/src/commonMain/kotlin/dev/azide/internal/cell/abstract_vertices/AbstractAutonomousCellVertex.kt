package dev.azide.internal.cell.abstract_vertices

import dev.azide.internal.CommittableVertex
import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex.Observer
import dev.azide.internal.cell.CellVertex.ObserverHandle
import dev.azide.internal.cell.CellVertex.Update
import dev.azide.internal.cell.WarmCellVertex
import dev.azide.internal.utils.weak_bag.MutableBag
import kotlin.jvm.JvmInline

abstract class AbstractAutonomousCellVertex<ValueT>() : WarmCellVertex<ValueT>, CommittableVertex {
    @JvmInline
    private value class ObserverHandleImpl<ValueT>(
        val internalHandle: MutableBag.Handle<Observer<ValueT>>,
    ) : ObserverHandle

    private val _registeredObservers: MutableBag<Observer<ValueT>> = MutableBag()

    private var _ongoingUpdate: Update<ValueT>? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingUpdate: Update<ValueT>?
        get() = _ongoingUpdate

    final override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: Observer<ValueT>,
    ): ObserverHandle {
        val internalHandle = _registeredObservers.add(observer)

        if (_registeredObservers.size == 1) {
            onFirstObserverRegistered(
                propagationContext = propagationContext,
            )
        }

        return ObserverHandleImpl(
            internalHandle = internalHandle,
        )
    }

    final override fun unregisterObserver(
        handle: ObserverHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? ObserverHandleImpl<ValueT> ?: throw IllegalArgumentException("Invalid handle")

        _registeredObservers.remove(handleImpl.internalHandle)

        if (_registeredObservers.size == 0) {
            onLastObserverUnregistered()
        }
    }

    final override fun commit() {
        commit(
            ongoingUpdate = _ongoingUpdate,
        )

        _ongoingUpdate = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeAndPropagateUpdate(
        propagationContext: Transactions.PropagationContext,
        update: Update<ValueT>?,
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
        update: Update<ValueT>?,
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
        update: Update<ValueT>?,
    ) {
        _registeredObservers.forEach { observer ->
            observer.handleUpdate(
                propagationContext = propagationContext,
                update = update,
            )

            // Do not remove the observer
            false
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

    protected open fun commit(
        ongoingUpdate: Update<ValueT>?,
    ) {
    }
}
