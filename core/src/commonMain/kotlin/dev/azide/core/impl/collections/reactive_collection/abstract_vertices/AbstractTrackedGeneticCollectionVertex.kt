package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.AbstractLiveVertex
import dev.azide.core.impl.Committable
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_set.SetChange

abstract class AbstractTrackedGenericCollectionVertex<ContentT : Collection<*>, ChangeT : GenericCollectionChange<*>>() :
    AbstractLiveVertex(), TrackedGenericCollectionVertex<ContentT, ChangeT>, Committable {
    private var _ongoingChange: ChangeT? = null

    private var _commitmentRevocable: Revocable? = null

    final override val ongoingChange: ChangeT?
        get() = _ongoingChange

    /**
     * If [change] is non-null (effective), expose it and notify listeners. If [change] is null (ineffective),
     * expose a revocation and notify listeners only if there's an ongoing change to revoke.
     */
    protected fun exposeChangeNotifyingListeners(
        propagationContext: Transactions.PropagationContext,
        change: ChangeT?,
    ) {
        val wasEffective = exposeChange(
            propagationContext = propagationContext,
            change = change,
        )

        if (wasEffective) {
            notifyListeners(
                propagationContext = propagationContext,
            )
        }
    }

    protected fun exposeChange(
        propagationContext: Transactions.PropagationContext,
        change: ChangeT?,
    ): Boolean {
        if (_ongoingChange == change) {
            return false
        }

        _ongoingChange = change

        if (_commitmentRevocable == null && change != null) { // Initial change (at least first after revocation)
            _commitmentRevocable = propagationContext.enqueueForCommitment(this)
        }

        val commitmentRevocable = this._commitmentRevocable

        if (commitmentRevocable != null && change == null) { // Change revocation
            commitmentRevocable.revoke()

            this._commitmentRevocable = null
        }

        return true
    }

    protected fun clearExposedChange() {
        _ongoingChange = null
    }

    final override fun commit(
        commitmentContext: Transactions.CommitmentContext,
    ) {
        val ongoingChange = this._ongoingChange

        persist(
            ongoingChange = ongoingChange,
        )

        transit()

        this._commitmentRevocable = null
        this._ongoingChange = null
    }

    protected open fun persist(
        ongoingChange: ChangeT?,
    ) {
    }

    protected open fun transit(
    ) {
    }
}

typealias AbstractTrackedSetVertex<ElementT> = AbstractTrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>

typealias AbstractTrackedListVertex<ElementT> = AbstractTrackedGenericCollectionVertex<List<ElementT>, ListChange<ElementT>>

typealias AbstractTrackedTaggedBagVertex<ElementT> = AbstractTrackedGenericCollectionVertex<TaggedBag<ElementT>, TaggedBagChange<ElementT>>
