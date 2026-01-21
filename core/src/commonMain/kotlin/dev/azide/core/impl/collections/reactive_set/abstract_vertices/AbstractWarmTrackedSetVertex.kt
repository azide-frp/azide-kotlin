package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.Listener
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.WarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers.TrackedSetSizeWarmCellVertex
import dev.kmpx.collections.StableCollection
import dev.kmpx.collections.lists.LinkedList
import kotlin.jvm.JvmInline

abstract class AbstractWarmTrackedSetVertex<ElementT>() : WarmTrackedSetVertex<ElementT>, CommittableVertex {
    @JvmInline
    private value class ListenerHandleImpl(
        val internalHandle: StableCollection.Handle<Listener>,
    ) : ListenerHandle

    private val _registeredListeners: LinkedList<Listener> = LinkedList()

    private var _ongoingChange: SetChange<ElementT>? = null

    private var _isEnqueuedForCommitment = false

    override fun registerListener(
        propagationContext: PropagationContext,
        listener: Listener,
    ): ListenerHandle {
        val internalHandle = _registeredListeners.append(listener)

        if (_registeredListeners.size == 1) {
            onFirstListenerRegistered(
                propagationContext = propagationContext,
            )
        }

        return ListenerHandleImpl(
            internalHandle = internalHandle,
        )
    }

    final override fun unregisterListener(
        handle: ListenerHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? ListenerHandleImpl ?: throw IllegalArgumentException("Invalid handle")

        _registeredListeners.removeVia(handleImpl.internalHandle)

        if (_registeredListeners.isEmpty()) {
            onLastListenerUnregistered()
        }
    }

    final override fun buildSizeVertex(): CellVertex<Int> = TrackedSetSizeWarmCellVertex(
        sourceVertex = this,
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

    private fun notifyListeners(
        propagationContext: PropagationContext,
    ) {
        _registeredListeners.forEach { listener ->
            listener.handle(
                propagationContext = propagationContext,
            )
        }
    }

    protected open fun onFirstListenerRegistered(
        propagationContext: PropagationContext,
    ) {
    }

    protected open fun onLastListenerUnregistered() {
    }

    protected open fun commit(
        ongoingChange: SetChange<ElementT>?,
    ) {
    }
}
