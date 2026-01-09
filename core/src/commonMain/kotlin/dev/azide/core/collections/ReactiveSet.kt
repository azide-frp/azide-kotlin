package dev.azide.core.collections

import dev.azide.core.Cell
import dev.azide.core.impl.collections.reactive_set.PureReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.FilteredWarmReactiveSetVertex

interface ReactiveSet<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: Set<ElementT>,
    ) : ReactiveSet<ElementT> {
        override val vertex: ReactiveSetVertex<ElementT> = PureReactiveSetVertex(
            elements = constElements,
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val vertex: ReactiveSetVertex<ElementT>,
    ) : ReactiveSet<ElementT>

    override val vertex: ReactiveSetVertex<ElementT>
}

fun <ElementT> ReactiveSet<ElementT>.contains(
    element: @UnsafeVariance ElementT,
): Cell<Boolean> = Cell.Ordinary(
    vertex = vertex.buildContainsVertex(element = element),
)

fun <ElementT> ReactiveSet<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveSet<ElementT> = ReactiveSet.Ordinary(
    FilteredWarmReactiveSetVertex(
        this@filter.vertex,
        predicate,
    ),
)
