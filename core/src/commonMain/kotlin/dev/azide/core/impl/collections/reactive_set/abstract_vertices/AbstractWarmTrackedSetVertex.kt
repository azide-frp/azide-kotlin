package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChangeNotificationObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.WarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers.TrackedSetContainsCellVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers.TrackedSetSizeWarmCellVertex
import dev.kmpx.collections.StableCollection
import dev.kmpx.collections.lists.LinkedList
import kotlin.jvm.JvmInline

abstract class AbstractWarmTrackedSetVertex<ElementT>() : WarmTrackedSetVertex<ElementT>, CommittableVertex {
    @JvmInline
    private value class ObserverHandleImpl(
        val internalHandle: StableCollection.Handle<CollectionChangeNotificationObserver>,
    ) : CollectionObserverHandle

    private val _registeredObservers: LinkedList<CollectionChangeNotificationObserver> = LinkedList()

    private var _ongoingChange: SetChange<ElementT>? = null

    private var _isEnqueuedForCommitment = false

    override fun registerCollectionNotificationObserver(
        propagationContext: PropagationContext,
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

    final override fun buildSizeVertex(): CellVertex<Int> = TrackedSetSizeWarmCellVertex(
        sourceVertex = this,
    )

    final override fun buildContainsVertex(
        element: ElementT,
    ): CellVertex<Boolean> = TrackedSetContainsCellVertex(
        sourceVertex = this,
        element = element,
    )

    final override val ongoingChange: SetChange<ElementT>?
        get() = _ongoingChange

    final override fun commit() {
        commit(
            ongoingChange = _ongoingChange,
        )

        _ongoingChange = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeAndPropagateChange(
        propagationContext: PropagationContext,
        change: SetChange<ElementT>?,
    ) {
        exposeChange(
            propagationContext = propagationContext,
            change = change,
        )

        propagateChange(
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

    private fun propagateChange(
        propagationContext: PropagationContext,
    ) {
        _registeredObservers.forEach { observer ->
            observer.handleChangeNotification(
                propagationContext = propagationContext,
            )
        }
    }

    protected open fun onFirstObserverRegistered(
        propagationContext: PropagationContext,
    ) {
    }

    protected open fun onLastObserverUnregistered() {
    }

    protected open fun commit(
        ongoingChange: SetChange<ElementT>?,
    ) {
    }
}
