package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChangeNotificationObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionSizeWarmCellVertex
import dev.kmpx.collections.StableCollection
import dev.kmpx.collections.lists.LinkedList
import kotlin.jvm.JvmInline

abstract class AbstractWarmTrackedCollectionVertex<ElementT>() : TrackedCollectionVertex<ElementT>,
    CommittableVertex {
    @JvmInline
    private value class ObserverHandleImpl(
        val internalHandle: StableCollection.Handle<CollectionChangeNotificationObserver>,
    ) : CollectionObserverHandle

    private val _registeredObservers: LinkedList<CollectionChangeNotificationObserver> = LinkedList()

    private var _ongoingChange: CollectionChange<ElementT>? = null

    private var _isEnqueuedForCommitment = false

    final override fun registerCollectionNotificationObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CollectionChangeNotificationObserver,
    ): CollectionObserverHandle {
        val internalHandle = _registeredObservers.append(observer)

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
            handle as? ObserverHandleImpl ?: throw IllegalArgumentException("Invalid handle")

        _registeredObservers.removeVia(handleImpl.internalHandle)

        if (_registeredObservers.isEmpty()) {
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

        propagateChangeNotification(
            propagationContext = propagationContext,
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

    private fun propagateChangeNotification(
        propagationContext: Transactions.PropagationContext,
    ) {
        _registeredObservers.forEach { observer ->
            observer.handleChangeNotification(
                propagationContext = propagationContext,
            )
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
