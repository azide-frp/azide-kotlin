package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.WarmCellVertex
import dev.azide.core.impl.cell.registerUpdateObserver
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex

/**
 * This vertex is unusual: although its operator is semantically stateful, it does not require the usual logic for
 * maintaining stateful entities. Its state stabilizes after the spawning transaction and remains observably stateless
 * thereafter, so extending [AbstractStatelessEventStreamVertex] is appropriate.
 */
class ValuesEventStreamVertex<ValueT> private constructor(
    propagationContext: Transactions.PropagationContext,
    private val sourceVertex: CellVertex<ValueT>,
) : AbstractSimpleStatelessEventStreamVertex<ValueT>(), UpdateObserver<ValueT> {
    private enum class InternalState {
        Spawning, Spawned,
    }

    companion object {
        fun <ValueT> start(
            propagationContext: Transactions.PropagationContext,
            sourceVertex: CellVertex<ValueT>,
        ): ValuesEventStreamVertex<ValueT> = ValuesEventStreamVertex(
            propagationContext = propagationContext,
            sourceVertex = sourceVertex,
        )
    }

    private var internalState = InternalState.Spawning

    private var upstreamObserverHandle: CellVertex.ObserverHandle? = null

    init {
        // Enqueue for commitment to ensure we observe the internal state switches to "spawned"
        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    /**
     * Handle the emission of the source cell vertex.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        when (update) {
            null -> { // Update revocation
                when (internalState) {
                    InternalState.Spawning -> { // Fall back to emitting the old value
                        val oldValue: ValueT = sourceVertex.getOldValue(
                            propagationContext = propagationContext,
                        )

                        exposeAndPropagateEmission(
                            propagationContext = propagationContext,
                            emission = EventStreamVertex.Emission(
                                emittedEvent = oldValue,
                            ),
                        )
                    }

                    InternalState.Spawned -> { // Just revoke the emission
                        exposeAndPropagateEmission(
                            propagationContext = propagationContext,
                            emission = null,
                        )
                    }
                }
            }

            else -> { // Initial update or correction
                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = EventStreamVertex.Emission(
                        emittedEvent = update.updatedValue,
                    ),
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.Emission<ValueT>? {
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerUpdateObserver(
            propagationContext = propagationContext,
            observer = this,
            mode = mode,
        )

        val sourceOngoingUpdate = sourceVertex.ongoingUpdate

        return when (internalState) {
            InternalState.Spawning -> { // Emit the new (updated / old) value
                val newValue: ValueT = when (sourceOngoingUpdate) {
                    null -> sourceVertex.getOldValue(
                        propagationContext = propagationContext,
                    )

                    else -> sourceOngoingUpdate.updatedValue
                }

                EventStreamVertex.Emission(
                    emittedEvent = newValue,
                )
            }

            InternalState.Spawned -> { // Emit the updated value if present
                sourceOngoingUpdate?.let { update ->
                    EventStreamVertex.Emission(
                        emittedEvent = update.updatedValue,
                    )
                }
            }
        }
    }

    override fun deactivate() {
        val upstreamObserverHandle =
            this.upstreamObserverHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterObserver(
            handle = upstreamObserverHandle,
        )

        this.upstreamObserverHandle = null
    }

    override fun transit() {
        internalState = InternalState.Spawned
    }
}
