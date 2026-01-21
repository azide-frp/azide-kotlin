package dev.azide.core.impl.collections.reactive_collection.operated_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractStatelessWarmTrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.map

class MappedWarmTrackedCollectionVertex<ElementT, TransformedElementT>(
    private val sourceVertex: TrackedCollectionVertex<ElementT>,
    private val transform: (ElementT) -> TransformedElementT,
) : AbstractStatelessWarmTrackedCollectionVertex<TransformedElementT>(),
    TrackedCollectionVertex.CollectionObserver<ElementT> {
    private var upstreamObserverHandle: TrackedCollectionVertex.CollectionObserverHandle? = null

    /**
     * Handle the change of the source reactive collection.
     */
    override fun handleChange(
        propagationContext: PropagationContext,
        change: CollectionChange<ElementT>?,
    ) {
        when (change) {
            null -> {
                if (ongoingChange != null) {
                    exposeAndPropagateChange(
                        propagationContext = propagationContext,
                        change = null,
                    )
                }
            }

            else -> {
                exposeAndPropagateChange(
                    propagationContext = propagationContext,
                    change = change.map(transform),
                )
            }
        }
    }

    override fun activate(
        propagationContext: PropagationContext,
    ): CollectionChange<TransformedElementT>? {
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerCollectionObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        return sourceVertex.ongoingChange?.map(transform)
    }

    override fun deactivate() {
        val upstreamObserverHandle =
            this.upstreamObserverHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterCollectionObserver(
            handle = upstreamObserverHandle,
        )

        this.upstreamObserverHandle = null
    }

    override fun getOldContentView(
        propagationContext: PropagationContext,
    ): Collection<TransformedElementT> {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return oldContentView.map(transform)
    }
}