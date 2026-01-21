package dev.azide.core.impl.collections.reactive_set.operated_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetObserver
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractStatelessWarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.utils.LazyFilteredSet

class FilteredWarmTrackedSetVertex<ElementT>(
    private val sourceVertex: TrackedSetVertex<ElementT>,
    private val predicate: (ElementT) -> Boolean,
) : AbstractStatelessWarmTrackedSetVertex<ElementT>(), SetObserver<ElementT> {
    private var upstreamObserverHandle: TrackedSetVertex.SetObserverHandle? = null

    /**
     * Handle the change of the source reactive set.
     */
    override fun handleChange(
        propagationContext: PropagationContext,
        change: SetChange<ElementT>?,
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
        propagationContext: PropagationContext,
    ): SetChange<ElementT>? {
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
        propagationContext: PropagationContext,
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
