package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.AbstractLiveVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractTrackedGenericCollectionVertex<ContentT : Collection<*>, ChangeT : GenericCollectionChange<*>>() :
    AbstractLiveVertex(),
    TrackedGenericCollectionVertex<ContentT, ChangeT> {
    private var _ongoingChange: ChangeT? = null

    private var _isEnqueuedForCommitment = false

    final override val ongoingChange: ChangeT?
        get() = _ongoingChange

    protected fun exposeChangeNotifyingListeners(
        propagationContext: Transactions.PropagationContext,
        change: ChangeT?,
    ) {
        exposeChange(
            propagationContext = propagationContext,
            change = change,
        )

        notifyListeners(
            propagationContext = propagationContext,
        )
    }

    protected fun exposeChange(
        propagationContext: Transactions.PropagationContext,
        change: ChangeT?,
    ) {
        _ongoingChange = change

        if (!_isEnqueuedForCommitment) {
            propagationContext.enqueueCallbackForCommitment {
                commit(
                    ongoingChange = _ongoingChange,
                )

                _ongoingChange = null
                _isEnqueuedForCommitment = false
            }

            _isEnqueuedForCommitment = true
        }
    }

    protected fun clearExposedChange() {
        _ongoingChange = null
    }

    protected open fun commit(
        ongoingChange: ChangeT?,
    ) {
    }
}

typealias AbstractTrackedSetVertex<ElementT> = AbstractTrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>

typealias AbstractTrackedListVertex<ElementT> = AbstractTrackedGenericCollectionVertex<List<ElementT>, ListChange<ElementT>>

typealias AbstractTrackedTaggedBagVertex<ElementT> = AbstractTrackedGenericCollectionVertex<TaggedBag<ElementT>, TaggedBagChange<ElementT>>
