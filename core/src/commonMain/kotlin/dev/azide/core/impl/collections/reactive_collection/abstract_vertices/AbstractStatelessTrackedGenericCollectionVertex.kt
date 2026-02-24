package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractStatelessTrackedGenericCollectionVertex<ContentT : Collection<*>, ChangeT : GenericCollectionChange<*>> :
    AbstractTrackedGenericCollectionVertex<ContentT, ChangeT>() {
    final override fun onFirstListenerRegistered(
        processingContext: Transactions.ProcessingContext,
    ) {
        when (processingContext) {
            is Transactions.PropagationContext -> {
                val changeOnActivation = activate(
                    processingContext = processingContext,
                )

                exposeChange(
                    propagationContext = processingContext,
                    change = changeOnActivation,
                )
            }

            is Transactions.CommitmentContext -> {
                activate(
                    processingContext = processingContext,
                )
            }
        }
    }

    final override fun onLastListenerUnregistered() {
        deactivate()

        clearExposedChange()
    }

    abstract fun activate(
        processingContext: Transactions.ProcessingContext,
    ): ChangeT?

    abstract fun deactivate()
}

typealias AbstractStatelessTrackedSetVertex<ElementT> = AbstractStatelessTrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>

typealias AbstractStatelessTrackedTaggedBagVertex<ElementT> = AbstractStatelessTrackedGenericCollectionVertex<TaggedBag<ElementT>, TaggedBagChange<ElementT>>

typealias AbstractStatelessTrackedListVertex<ElementT> = AbstractStatelessTrackedGenericCollectionVertex<List<ElementT>, ListChange<ElementT>>
