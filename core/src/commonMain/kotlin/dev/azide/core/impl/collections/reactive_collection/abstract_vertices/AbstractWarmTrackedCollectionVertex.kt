package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionSizeWarmCellVertex
import dev.azide.core.impl.utils.weak_bag.MutableBag
import kotlin.jvm.JvmInline

abstract class AbstractWarmTrackedCollectionVertex<ElementT>() : TrackedCollectionVertex<ElementT>,
    CommittableVertex {
    @JvmInline
    private value class ObserverHandleImpl<ElementT>(
        val internalHandle: MutableBag.Handle<CollectionObserver<ElementT>>,
    ) : CollectionObserverHandle

    private val _registeredObservers: MutableBag<CollectionObserver<ElementT>> = MutableBag()

    private var _ongoingChange: CollectionChange<ElementT>? = null

    private var _isEnqueuedForCommitment = false

    final override fun registerCollectionObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CollectionObserver<ElementT>,
    ): CollectionObserverHandle {
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

    final override fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? ObserverHandleImpl<ElementT> ?: throw IllegalArgumentException("Invalid handle")

        _registeredObservers.remove(handleImpl.internalHandle)

        if (_registeredObservers.size == 0) {
            onLastObserverUnregistered()
        }
    }

    final override fun buildSizeVertex(): CellVertex<Int> = TrackedCollectionSizeWarmCellVertex(
        sourceVertex = this,
    )

    final override val ongoingChange: CollectionChange<ElementT>?
        get() = _ongoingChange

    final override fun commit() {
        commit(
            ongoingChange = _ongoingChange,
        )

        _ongoingChange = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeAndPropagateChange(
        propagationContext: Transactions.PropagationContext,
        change: CollectionChange<ElementT>?,
    ) {
        exposeChange(
            propagationContext = propagationContext,
            change = change,
        )

        propagateChange(
            propagationContext = propagationContext,
            change = change,
        )
    }

    protected fun exposeChange(
        propagationContext: Transactions.PropagationContext,
        change: CollectionChange<ElementT>?,
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

    private fun propagateChange(
        propagationContext: Transactions.PropagationContext,
        change: CollectionChange<ElementT>?,
    ) {
        _registeredObservers.forEach { observer ->
            observer.handleChange(
                propagationContext = propagationContext,
                change = change,
            )

            // Do not remove the observer
            false
        }
    }

    protected open fun onFirstObserverRegistered(
        propagationContext: Transactions.PropagationContext,
    ) {
    }

    protected open fun onLastObserverUnregistered() {
    }

    protected open fun commit(
        ongoingChange: CollectionChange<ElementT>?,
    ) {
    }
}
