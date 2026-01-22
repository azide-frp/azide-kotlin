package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractStatelessWarmTrackedSetVertex<ElementT> : AbstractWarmTrackedSetVertex<ElementT>() {
    final override fun onFirstListenerRegistered(
        propagationContext: PropagationContext,
        mode: ActivationMode,
    ) {
        val changeOnActivation = activate(
            propagationContext = propagationContext,
            mode = mode,
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
        mode: ActivationMode,
    ): SetChange<ElementT>?

    abstract fun deactivate()
}
