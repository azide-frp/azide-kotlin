package dev.azide.core.impl.collections.reactive_bag.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.collections.reactive_bag.MutableTaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange

abstract class AbstractStatefulTrackedTaggedBagVertex<ElementT>(
    wrapUpContext: Transactions.WrapUpContext,
    initialTaggedElements: MutableTaggedBag<ElementT>,
) : AbstractBaseStatefulTrackedTaggedBagVertex<ElementT>(
    initialTaggedElements = initialTaggedElements,
) {
    private var isInitialized = false

    final override fun onFirstListenerRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ) {
        if (isInitialized) return

        if (mode == Vertex.ActivationMode.Offline) {
            throw UnsupportedOperationException("Offline initialization is not supported")
        }

        ensureInitialized(
            propagationContext = propagationContext,
        )
    }

    final override fun onLastListenerUnregistered() {
    }

    protected abstract fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): TaggedBagChange<ElementT>?

    private fun ensureInitialized(
        propagationContext: Transactions.PropagationContext,
    ) {
        val changeOnInitialization = initialize(
            propagationContext = propagationContext,
        )

        exposeChange(
            propagationContext = propagationContext,
            change = changeOnInitialization,
        )

        isInitialized = true
    }

    init {
        wrapUpContext.enqueueForWrapUp { propagationContext ->
            if (isInitialized) return@enqueueForWrapUp

            ensureInitialized(
                propagationContext = propagationContext,
            )
        }
    }
}
