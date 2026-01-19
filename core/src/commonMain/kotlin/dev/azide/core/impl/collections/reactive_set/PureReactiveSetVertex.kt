package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractFrozenReactiveSetVertex

class PureReactiveSetVertex<ElementT>(
    val elements: Set<ElementT>,
) : AbstractFrozenReactiveSetVertex<ElementT>() {
    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Set<ElementT> = elements
}
