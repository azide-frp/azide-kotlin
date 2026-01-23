package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.applyTo

abstract class AbstractStatefulTrackedSetVertex<ElementT>(
    initialElements: MutableSet<ElementT>,
) : AbstractTrackedSetVertex<ElementT>() {
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
