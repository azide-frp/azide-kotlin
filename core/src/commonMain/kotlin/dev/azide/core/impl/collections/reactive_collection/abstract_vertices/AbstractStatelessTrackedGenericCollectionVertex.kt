package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractStatelessTrackedGenericCollectionVertex<ContentT : Collection<*>, ChangeT : GenericCollectionChange<*>> :
    AbstractTrackedGenericCollectionVertex<ContentT, ChangeT>() {
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

typealias AbstractStatelessTrackedTaggedBagVertex<ElementT> = AbstractStatelessTrackedGenericCollectionVertex<TaggedBag<ElementT>, TaggedBagChange<ElementT>>

typealias AbstractStatelessTrackedListVertex<ElementT> = AbstractStatelessTrackedGenericCollectionVertex<List<ElementT>, ListChange<ElementT>>
