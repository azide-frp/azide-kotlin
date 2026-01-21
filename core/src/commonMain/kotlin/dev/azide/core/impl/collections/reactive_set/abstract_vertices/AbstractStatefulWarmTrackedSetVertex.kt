package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.utils.applyTo

abstract class AbstractStatefulWarmTrackedSetVertex<ElementT>(
    initialElements: MutableSet<ElementT>,
) : AbstractWarmTrackedSetVertex<ElementT>() {
    private var _stableElements: MutableSet<ElementT> = initialElements

    final override fun commit(
        ongoingChange: SetChange<ElementT>?,
    ) {
        ongoingChange?.applyTo(_stableElements)
    }

    final override fun getOldContentView(
        propagationContext: PropagationContext,
    ): Set<ElementT> = _stableElements
}
