package dev.azide.core.collections

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.impl.collections.reactive_collection.PureTrackedSetVertex
import dev.azide.core.impl.collections.reactive_collection.buildContainsVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.FilteredWarmTrackedSetVertex

interface ReactiveSet<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: Set<ElementT>,
    ) : ReactiveSet<ElementT> {
        override val trackedVertex: TrackedSetVertex<ElementT> = PureTrackedSetVertex(
            elements = constElements,
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val trackedVertex: TrackedSetVertex<ElementT>,
    ) : ReactiveSet<ElementT>

    override val trackedVertex: TrackedSetVertex<ElementT>
}

val <ElementT> ReactiveSet<ElementT>.asReactiveBag: ReactiveBag<ElementT>
    get() = TODO()

fun <ElementT> ReactiveSet<ElementT>.contains(
    element: ElementT,
): Cell<Boolean> = Cell.Ordinary(
    vertex = trackedVertex.buildContainsVertex(element = element),
)

fun <ElementT> ReactiveSet<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveSet<ElementT> = ReactiveSet.Ordinary(
    trackedVertex = FilteredWarmTrackedSetVertex(
        sourceVertex = this@filter.trackedVertex,
        predicate = predicate,
    ),
)

fun <ElementT, TransformedElementT> ReactiveSet<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ReactiveBag<TransformedElementT> = TODO()

fun <ElementT> ReactiveSet<ElementT>.fuseOf(
    selector: (ElementT) -> Cell<ElementT>,
): ReactiveBag<ElementT> = map(selector).fuse()

fun <ElementT, ResultT> ReactiveSet<ElementT>.executeEveryOf(
    selector: (ElementT) -> Action<ResultT>,
): Effect<ReactiveBag<ResultT>> = map(selector).executeEvery()

fun <ElementT, ResultT> ReactiveSet<ElementT>.actuateOf(
    selector: (ElementT) -> Effect<ResultT>,
): Effect<ReactiveBag<ResultT>> = map(selector).actuate()
