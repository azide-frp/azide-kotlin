package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex.CollectionChange

abstract class AbstractStatelessWarmReactiveCollectionVertex<ElementT> :
    AbstractWarmReactiveCollectionVertex<ElementT>() {
    final override fun onFirstObserverRegistered(
        propagationContext: Transactions.PropagationContext,
    ) {
        val changeOnActivation = activate(
            propagationContext = propagationContext,
        )

        exposeChange(
            propagationContext = propagationContext,
            change = changeOnActivation,
        )
    }

    final override fun onLastObserverUnregistered() {
        deactivate()

        clearExposedChange()
    }

    abstract fun activate(
        propagationContext: Transactions.PropagationContext,
    ): CollectionChange<ElementT>?

    abstract fun deactivate()
}
