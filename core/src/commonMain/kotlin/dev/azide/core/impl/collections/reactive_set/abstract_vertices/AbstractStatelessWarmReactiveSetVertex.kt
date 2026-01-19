package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex

abstract class AbstractStatelessWarmReactiveSetVertex<ElementT> : AbstractWarmReactiveSetVertex<ElementT>() {
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
    ): ReactiveSetVertex.SetChange<ElementT>?

    abstract fun deactivate()
}
