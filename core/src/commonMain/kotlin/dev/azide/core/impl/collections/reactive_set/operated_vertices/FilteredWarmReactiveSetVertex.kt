package dev.azide.core.impl.collections.reactive_set.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.WarmReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractStatelessWarmReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.utils.LazyFilteredSet

class FilteredWarmReactiveSetVertex<ElementT>(
    private val sourceVertex: ReactiveSetVertex<ElementT>,
    private val predicate: (ElementT) -> Boolean,
) : AbstractStatelessWarmReactiveSetVertex<ElementT>(), ReactiveSetVertex.SetObserver<ElementT> {
    private var upstreamObserverHandle: ReactiveSetVertex.SetObserverHandle? = null

    /**
     * Handle the change of the source reactive set.
     */
    override fun handleChange(
        propagationContext: Transactions.PropagationContext,
        change: ReactiveSetVertex.SetChange<ElementT>?,
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
                when (val filteredChange = change.filter(predicate)) {
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
                            change = filteredChange,
                        )
                    }
                }
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): ReactiveSetVertex.SetChange<ElementT>? {
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerSetObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        return sourceVertex.ongoingChange?.filter(predicate)
    }

    override fun deactivate() {
        val upstreamObserverHandle =
            this.upstreamObserverHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterSetObserver(
            handle = upstreamObserverHandle,
        )

        this.upstreamObserverHandle = null
    }

    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Set<ElementT> {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return LazyFilteredSet(
            sourceSet = oldContentView,
            predicate = predicate,
        )
    }
}
