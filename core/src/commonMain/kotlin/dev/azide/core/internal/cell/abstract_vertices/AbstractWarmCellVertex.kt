package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.CommittableVertex
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.utils.weak_bag.MutableBag
import dev.azide.core.internal.cell.CellVertex.Observer
import kotlin.jvm.JvmInline

abstract class AbstractWarmCellVertex<ValueT>() : WarmCellVertex<ValueT>, CommittableVertex {
    @JvmInline
    private value class ObserverHandleImpl<ValueT>(
        val internalHandle: MutableBag.Handle<Observer<ValueT>>,
    ) : CellVertex.ObserverHandle

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

        return ObserverHandleImpl(
            internalHandle = internalHandle,
        )
    }

    final override fun unregisterObserver(
        handle: CellVertex.ObserverHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? ObserverHandleImpl<ValueT> ?: throw IllegalArgumentException("Invalid handle")

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

    protected open fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
    }

    protected open fun transit(
    ) {
    }
}
