package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.utils.applyTo

abstract class AbstractStatefulWarmReactiveSetVertex<ElementT>(
    initialElements: MutableSet<ElementT>,
) : AbstractWarmReactiveSetVertex<ElementT>() {
    private var _stableElements: MutableSet<ElementT> = initialElements

    final override fun commit(
        ongoingChange: ReactiveSetVertex.SetChange<ElementT>?,
    ) {
        ongoingChange?.applyTo(_stableElements)
    }

    final override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Set<ElementT> = _stableElements
}
