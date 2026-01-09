package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex

abstract class AbstractReactiveCollectionProxyCellVertex<ElementT, ValueT>(
    private val sourceVertex: ReactiveCollectionVertex<ElementT>,
) : AbstractCachingCellVertex<ValueT>(
    cacheType = CacheType.Active,
), ReactiveCollectionVertex.CollectionObserver<ElementT> {
    private var upstreamObserverHandle: ReactiveCollectionVertex.CollectionObserverHandle? = null

    final override fun handleChange(
        propagationContext: Transactions.PropagationContext,
        change: ReactiveCollectionVertex.CollectionChange<ElementT>?,
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
        propagationContext: Transactions.PropagationContext,
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
                propagationContext = propagationContext, sourceChange = sourceOngoingChange
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
        propagationContext: Transactions.PropagationContext,
    ): ValueT {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return computeOldValue(
            oldContentView = oldContentView,
        )
    }

    abstract fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceChange: ReactiveCollectionVertex.CollectionChange<ElementT>,
    ): CellVertex.Update<ValueT>?

    abstract fun computeOldValue(
        oldContentView: Collection<ElementT>,
    ): ValueT
}
