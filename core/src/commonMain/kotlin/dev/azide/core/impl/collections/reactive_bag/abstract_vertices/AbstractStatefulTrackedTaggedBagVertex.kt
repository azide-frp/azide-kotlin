package dev.azide.core.impl.collections.reactive_bag.abstract_vertices

import dev.azide.core.impl.Transactions
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
        processingContext: Transactions.ProcessingContext,
    ) {
        if (isInitialized) return

        when (processingContext) {
            is Transactions.PropagationContext -> {
                ensureInitialized(
                    propagationContext = processingContext,
                )
            }

            is Transactions.CommitmentContext -> {
                throw UnsupportedOperationException("Offline initialization is not supported")
            }
        }
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
