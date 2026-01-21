package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChangeNotificationObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionObserverHandle

abstract class AbstractTrackedGenericCollectionProxyCellVertex<ContentT : Collection<*>, ChangeT : CollectionChange<*>, ValueT>(
    private val sourceVertex: TrackedGenericCollectionVertex<ContentT, ChangeT>,
) : AbstractCachingCellVertex<ValueT>(
    cacheType = CacheType.Active,
), CollectionChangeNotificationObserver {
    private var upstreamObserverHandle: CollectionObserverHandle? = null

    override fun handleChangeNotification(
        propagationContext: PropagationContext,
    ) {
        when (val change = sourceVertex.ongoingChange) {
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

        upstreamObserverHandle = sourceVertex.registerCollectionNotificationObserver(
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

    protected abstract fun buildUpdate(
        propagationContext: PropagationContext,
        sourceChange: ChangeT,
    ): CellVertex.Update<ValueT>?

    protected abstract fun computeOldValue(
        oldContentView: ContentT,
    ): ValueT
}

typealias AbstractTrackedCollectionProxyCellVertex<ElementT, ValueT> = AbstractTrackedGenericCollectionProxyCellVertex<Collection<ElementT>, CollectionChange<ElementT>, ValueT>
