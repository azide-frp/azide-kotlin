package dev.azide.core.impl.collections.reactive_set.operated_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractStatelessWarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.utils.LazyFilteredSet
import dev.azide.core.impl.registerBoundListener

class FilteredWarmTrackedSetVertex<ElementT>(
    private val sourceVertex: TrackedSetVertex<ElementT>,
    private val predicate: (ElementT) -> Boolean,
) : AbstractStatelessWarmTrackedSetVertex<ElementT>(), Vertex.BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

    /**
     * Handle the change of the source reactive set.
     */
    override fun handle(
        propagationContext: PropagationContext,
    ) {
        when (val change = sourceVertex.ongoingChange) {
            null -> {
                if (ongoingChange != null) {
                    exposeChangeNotifyingListeners(
                        propagationContext = propagationContext,
                        change = null,
                    )
                }
            }

            else -> {
                when (val filteredChange = change.filter(predicate)) {
                    null -> {
                        if (ongoingChange != null) {
                            exposeChangeNotifyingListeners(
                                propagationContext = propagationContext,
                                change = null,
                            )
                        }
                    }

                    else -> {
                        exposeChangeNotifyingListeners(
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
        mode: Vertex.ActivationMode,
    ): SetChange<ElementT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingChange?.filter(predicate)
    }

    override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
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
