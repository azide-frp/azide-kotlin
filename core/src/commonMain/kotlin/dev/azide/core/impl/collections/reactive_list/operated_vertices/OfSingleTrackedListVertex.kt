package dev.azide.core.impl.collections.reactive_list.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractStatelessTrackedListVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.registerBoundListener

class OfSingleTrackedListVertex<ElementT>(
    private val sourceVertex: CellVertex<ElementT>,
) : AbstractStatelessTrackedListVertex<ElementT>(), BoundListener {
    companion object {
        private fun <ElementT> buildChange(
            sourceUpdate: CellVertex.Update<ElementT>,
        ): ListChange<ElementT> = ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 0,
                    lastIndexExclusive = 1,
                    newElements = listOf(sourceUpdate.updatedValue),
                )
            ),
        )
    }

    private var upstreamListenerHandle: ListenerHandle? = null

    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val sourceUpdate = sourceVertex.ongoingUpdate) {
            null -> {
                if (ongoingChange != null) {
                    exposeChangeNotifyingListeners(
                        propagationContext = propagationContext,
                        change = null,
                    )
                }
            }

            else -> {
                val builtChange = buildChange(
                    sourceUpdate = sourceUpdate,
                )

                exposeChangeNotifyingListeners(
                    propagationContext = propagationContext,
                    change = builtChange,
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: ListenableVertex.ActivationMode,
    ): ListChange<ElementT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingUpdate?.let { sourceOngoingUpdate ->
            buildChange(sourceOngoingUpdate)
        }
    }

    override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
    }

    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): List<ElementT> {
        val sourceValue = sourceVertex.getOldValue(
            propagationContext = propagationContext,
        )

        return listOf(sourceValue)
    }
}
