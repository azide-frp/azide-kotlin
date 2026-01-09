package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex.CollectionObserver
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex.SetObserver
import dev.azide.core.impl.collections.reactive_set.WarmReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers.ReactiveSetContainsCellVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers.ReactiveSetSizeWarmCellVertex
import dev.azide.core.impl.utils.weak_bag.MutableBag
import kotlin.jvm.JvmInline

abstract class AbstractWarmReactiveSetVertex<ElementT>() : WarmReactiveSetVertex<ElementT>, CommittableVertex {
    @JvmInline
    private value class ObserverHandleImpl<ElementT>(
        val internalHandle: MutableBag.Handle<SetObserver<ElementT>>,
    ) : ReactiveSetVertex.SetObserverHandle

    private val _registeredObservers: MutableBag<SetObserver<ElementT>> = MutableBag()

    private var _ongoingChange: SetChange<ElementT>? = null

    private var _isEnqueuedForCommitment = false

    final override fun registerCollectionObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CollectionObserver<ElementT>,
    ): CollectionObserverHandle = registerSetObserver(
        propagationContext = propagationContext,
        observer = observer,
    )

    final override fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    ) {
        unregisterObserverImpl(
            handle = handle,
        )
    }

    final override fun buildSizeVertex(
    ): CellVertex<Int> = ReactiveSetSizeWarmCellVertex(
        sourceVertex = this,
    )

    final override fun buildContainsVertex(
        element: ElementT,
    ): CellVertex<Boolean> = ReactiveSetContainsCellVertex(
        sourceVertex = this,
        element = element,
    )

    final override val ongoingChange: SetChange<ElementT>?
        get() = _ongoingChange

    final override fun registerSetObserver(
        propagationContext: Transactions.PropagationContext,
        observer: SetObserver<ElementT>,
    ): ReactiveSetVertex.SetObserverHandle = registerObserverImpl(
        propagationContext = propagationContext,
        observer = observer,
    )

    final override fun unregisterSetObserver(
        handle: ReactiveSetVertex.SetObserverHandle,
    ) {
        unregisterObserverImpl(handle)
    }

    final override fun commit() {
        commit(
            ongoingChange = _ongoingChange,
        )

        _ongoingChange = null
        _isEnqueuedForCommitment = false
    }

    protected fun exposeAndPropagateChange(
        propagationContext: Transactions.PropagationContext,
        change: SetChange<ElementT>?,
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

    private fun registerObserverImpl(
        propagationContext: Transactions.PropagationContext,
        observer: SetObserver<ElementT>,
    ): ObserverHandleImpl<ElementT> {
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

    private fun unregisterObserverImpl(
        handle: CollectionObserverHandle,
    ) {
        @Suppress("UNCHECKED_CAST") val handleImpl =
            handle as? ObserverHandleImpl<ElementT> ?: throw IllegalArgumentException("Invalid handle")

        _registeredObservers.remove(handleImpl.internalHandle)

        if (_registeredObservers.size == 0) {
            onLastObserverUnregistered()
        }
    }

    private fun propagateChange(
        propagationContext: Transactions.PropagationContext,
        change: SetChange<ElementT>?,
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
        ongoingChange: SetChange<ElementT>?,
    ) {
    }
}
