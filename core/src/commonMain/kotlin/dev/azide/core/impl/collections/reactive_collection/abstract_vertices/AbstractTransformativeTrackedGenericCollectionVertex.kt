package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.registerBoundListener

abstract class AbstractTransformativeTrackedGenericCollectionVertex<ContentT : Collection<*>, ChangeT : CollectionChange<*>> :
    AbstractStatelessTrackedGenericCollectionVertex<ContentT, ChangeT>(), BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

    /**
     * Handle the change of the source reactive collection.
     */
    final override fun handle(
        propagationContext: PropagationContext,
    ) {
        when (val sourceOngoingChange: ChangeT? = sourceVertex.ongoingChange) {
            null -> { // Revocation
                if (ongoingChange != null) {
                    exposeChangeNotifyingListeners(
                        propagationContext = propagationContext,
                        change = null,
                    )
                }
            }

            else -> { // Original change / change correction
                when (val transformedChange = transformChange(sourceOngoingChange)) {
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
                            change = transformedChange,
                        )
                    }
                }
            }
        }
    }

    final override fun activate(
        propagationContext: PropagationContext,
        mode: Vertex.ActivationMode,
    ): ChangeT? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingChange?.let(::transformChange)
    }

    final override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
    }

    final override fun getOldContentView(
        propagationContext: PropagationContext,
    ): ContentT {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return transformOldContentView(
            oldContentView = oldContentView,
        )
    }

    protected abstract val sourceVertex: TrackedGenericCollectionVertex<ContentT, ChangeT>

    protected abstract fun transformOldContentView(
        oldContentView: ContentT,
    ): ContentT

    protected abstract fun transformChange(
        change: ChangeT,
    ): ChangeT?
}

typealias AbstractTransformativeTrackedSetVertex<ElementT> = AbstractTransformativeTrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>
