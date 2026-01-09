package dev.azide.core.impl.collections.reactive_set.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex.CollectionObserver
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_collection.map
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractStatelessWarmReactiveCollectionVertex

class MappedWarmReactiveCollectionVertex<ElementT, TransformedElementT>(
    private val sourceVertex: ReactiveCollectionVertex<ElementT>,
    private val transform: (ElementT) -> TransformedElementT,
) : AbstractStatelessWarmReactiveCollectionVertex<TransformedElementT>(), CollectionObserver<ElementT> {
    private var upstreamObserverHandle: CollectionObserverHandle? = null

    /**
     * Handle the change of the source reactive collection.
     */
    override fun handleChange(
        propagationContext: Transactions.PropagationContext,
        change: ReactiveCollectionVertex.CollectionChange<ElementT>?,
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
        propagationContext: Transactions.PropagationContext,
    ): ReactiveCollectionVertex.CollectionChange<TransformedElementT>? {
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
        propagationContext: Transactions.PropagationContext,
    ): Collection<TransformedElementT> {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return oldContentView.map(transform)
    }
}
