package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.registerBoundListener

abstract class AbstractTrackedGenericCollectionProxyCellVertex<ContentT : Collection<*>, ChangeT : GenericCollectionChange<*>, ValueT>(
    private val sourceVertex: TrackedGenericCollectionVertex<ContentT, ChangeT>,
) : AbstractCachingCellVertex<ValueT>(
), BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

    override fun handle(
        propagationContext: PropagationContext,
    ) {
        when (val change = sourceVertex.ongoingChange) {
            null -> {
                if (ongoingUpdate != null) {
                    exposeUpdateNotifyingListeners(
                        propagationContext = propagationContext,
                        update = null,
                    )
                }
            }

            else -> {
                val builtUpdate = buildUpdate(
                    propagationContext = propagationContext,
                    sourceVertex = sourceVertex,
                    sourceChange = change,
                )

                when (builtUpdate) {
                    null -> {
                        if (ongoingUpdate != null) {
                            exposeUpdateNotifyingListeners(
                                propagationContext = propagationContext,
                                update = builtUpdate,
                            )
                        }
                    }

                    else -> {
                        exposeUpdateNotifyingListeners(
                            propagationContext = propagationContext,
                            update = builtUpdate,
                        )
                    }
                }
            }
        }
    }

    override fun activate(
        processingContext: Transactions.ProcessingContext,
    ) {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            processingContext = processingContext,
            listener = this,
        )
    }

    override fun buildInitialUpdate(
        propagationContext: PropagationContext,
    ): CellVertex.Update<ValueT>? = sourceVertex.ongoingChange?.let { sourceOngoingChange ->
        buildUpdate(
            propagationContext = propagationContext,
            sourceVertex = sourceVertex,
            sourceChange = sourceOngoingChange,
        )
    }

    final override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
    }

    final override fun computeOldValue(
        processingContext: Transactions.ProcessingContext,
    ): ValueT {
        val oldContentView = sourceVertex.getOldContentView(
            processingContext = processingContext,
        )

        return computeOldValue(
            oldContentView = oldContentView,
        )
    }

    protected abstract fun buildUpdate(
        propagationContext: PropagationContext,
        sourceVertex: TrackedGenericCollectionVertex<ContentT, ChangeT>,
        sourceChange: ChangeT,
    ): CellVertex.Update<ValueT>?

    protected abstract fun computeOldValue(
        oldContentView: ContentT,
    ): ValueT
}

typealias AbstractTrackedCollectionProxyCellVertex<ElementT, ValueT> = AbstractTrackedGenericCollectionProxyCellVertex<Collection<ElementT>, TrackedGenericCollectionVertex.CollectionChange<ElementT>, ValueT>

typealias AbstractTrackedSetProxyCellVertex<ElementT, ValueT> = AbstractTrackedGenericCollectionProxyCellVertex<Set<ElementT>, SetChange<ElementT>, ValueT>
