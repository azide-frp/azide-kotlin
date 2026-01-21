package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle

abstract class AbstractTrackedCollectionProxyCellVertex<ElementT, ValueT>(
    private val sourceVertex: TrackedCollectionVertex<ElementT>,
) : AbstractCachingCellVertex<ValueT>(
    cacheType = CacheType.Active,
), TrackedCollectionVertex.CollectionObserver<ElementT> {
    private var upstreamObserverHandle: CollectionObserverHandle? = null

    final override fun handleChange(
        propagationContext: PropagationContext,
        change: CollectionChange<ElementT>?,
    ) {
        when (change) {
            null -> {
                if (ongoingUpdate != null) {
                    exposeAndPropagateUpdate(
                        propagationContext = propagationContext,
                        update = null,
                    )
                }
            }

            else -> {
                val builtUpdate = buildUpdate(
                    propagationContext = propagationContext,
                    sourceChange = change,
                )

                when (builtUpdate) {
                    null -> {
                        if (ongoingUpdate != null) {
                            exposeAndPropagateUpdate(
                                propagationContext = propagationContext,
                                update = builtUpdate,
                            )
                        }
                    }

                    else -> {
                        exposeAndPropagateUpdate(
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
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerCollectionObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        return sourceVertex.ongoingChange?.let { sourceOngoingChange ->
            buildUpdate(
                propagationContext = propagationContext,
                sourceChange = sourceOngoingChange,
            )
        }
    }

    final override fun deactivate() {
        val upstreamObserverHandle =
            this.upstreamObserverHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterCollectionObserver(
            handle = upstreamObserverHandle,
        )

        this.upstreamObserverHandle = null
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

    abstract fun buildUpdate(
        propagationContext: PropagationContext,
        sourceChange: CollectionChange<ElementT>,
    ): CellVertex.Update<ValueT>?

    abstract fun computeOldValue(
        oldContentView: Collection<ElementT>,
    ): ValueT
}
