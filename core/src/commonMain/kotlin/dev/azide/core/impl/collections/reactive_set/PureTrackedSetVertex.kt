package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractFrozenTrackedSetVertex

class PureTrackedSetVertex<ElementT>(
    val elements: Set<ElementT>,
) : AbstractFrozenTrackedSetVertex<ElementT>() {
    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Set<ElementT> = elements
}
