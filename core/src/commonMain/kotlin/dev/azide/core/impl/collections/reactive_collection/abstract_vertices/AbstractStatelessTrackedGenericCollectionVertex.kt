package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractStatelessTrackedGenericCollectionVertex<ContentT : Collection<*>, ChangeT : CollectionChange<*>> :
    AbstractTrackedGeneticCollectionVertex<ContentT, ChangeT>() {
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
    ): ChangeT?

    abstract fun deactivate()
}

typealias AbstractStatelessTrackedSetVertex<ElementT> = AbstractStatelessTrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>
