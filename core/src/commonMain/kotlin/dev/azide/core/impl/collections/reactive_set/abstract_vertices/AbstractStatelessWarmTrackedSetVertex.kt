package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractStatelessWarmTrackedSetVertex<ElementT> : AbstractWarmTrackedSetVertex<ElementT>() {
    final override fun onFirstListenerRegistered(
        propagationContext: PropagationContext,
    ) {
        val changeOnActivation = activate(
            propagationContext = propagationContext,
        )

        exposeChange(
            propagationContext = propagationContext,
            change = changeOnActivation,
        )
    }

    final override fun onLastListenerUnregistered() {
        deactivate()

        clearExposedChange()
    }

    abstract fun activate(
        propagationContext: PropagationContext,
    ): SetChange<ElementT>?

    abstract fun deactivate()
}
