package dev.azide.core.collections

import dev.azide.core.Cell
import dev.azide.core.impl.collections.reactive_set.PureTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.FilteredWarmTrackedSetVertex

interface ReactiveSet<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: Set<ElementT>,
    ) : ReactiveSet<ElementT> {
        override val vertex: TrackedSetVertex<ElementT> = PureTrackedSetVertex(
            elements = constElements,
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val vertex: TrackedSetVertex<ElementT>,
    ) : ReactiveSet<ElementT>

    override val vertex: TrackedSetVertex<ElementT>
}

fun <ElementT> ReactiveSet<ElementT>.contains(
    element: @UnsafeVariance ElementT,
): Cell<Boolean> = Cell.Ordinary(
    vertex = vertex.buildContainsVertex(element = element),
)

fun <ElementT> ReactiveSet<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveSet<ElementT> = ReactiveSet.Ordinary(
    FilteredWarmTrackedSetVertex(
        this@filter.vertex,
        predicate,
    ),
)
