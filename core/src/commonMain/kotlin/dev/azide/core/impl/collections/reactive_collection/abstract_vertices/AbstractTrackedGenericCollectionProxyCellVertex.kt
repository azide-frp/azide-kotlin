package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.registerBoundListener

abstract class AbstractTrackedGenericCollectionProxyCellVertex<ContentT : Collection<*>, ChangeT : GenericCollectionChange<*>, ValueT>(
    private val sourceVertex: TrackedGenericCollectionVertex<ContentT, ChangeT>,
) : AbstractCachingCellVertex<ValueT>(
    cacheType = CacheType.Active,
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
        propagationContext: PropagationContext,
        mode: Vertex.ActivationMode,
    ): CellVertex.Update<ValueT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingChange?.let { sourceOngoingChange ->
            buildUpdate(
                propagationContext = propagationContext,
                sourceVertex = sourceVertex,
                sourceChange = sourceOngoingChange,
            )
        }
    }

    final override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
    }

    final override fun computeOldValue(
        propagationContext: PropagationContext,
    ): ValueT {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
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
