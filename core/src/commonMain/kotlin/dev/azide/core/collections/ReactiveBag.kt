package dev.azide.core.collections

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.impl.collections.reactive_collection.PureTrackedSetVertex
import dev.azide.core.impl.collections.reactive_collection.buildContainsVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.operated_vertices.FilteredWarmTrackedSetVertex

interface ReactiveBag<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: Set<ElementT>,
    ) : ReactiveBag<ElementT> {
        override val trackedVertex: TrackedSetVertex<ElementT> = PureTrackedSetVertex(
            elements = constElements,
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val trackedVertex: TrackedSetVertex<ElementT>,
    ) : ReactiveBag<ElementT>

    override val trackedVertex: TrackedSetVertex<ElementT>
}

fun <ElementT> ReactiveBag<ElementT>.contains(
    element: ElementT,
): Cell<Boolean> = Cell.Ordinary(
    vertex = trackedVertex.buildContainsVertex(element = element),
)

fun <ElementT> ReactiveBag<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveBag<ElementT> = ReactiveBag.Ordinary(
    trackedVertex = FilteredWarmTrackedSetVertex(
        this@filter.trackedVertex,
        predicate,
    ),
)

fun <ElementT, TransformedElementT> ReactiveBag<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ReactiveBag<TransformedElementT> = TODO()

fun <ElementT> ReactiveBag<Cell<ElementT>>.fuse(): ReactiveBag<ElementT> = TODO()

fun <ElementT> ReactiveBag<ElementT>.fuseOf(
    selector: (ElementT) -> Cell<ElementT>,
): ReactiveBag<ElementT> = map(selector).fuse()

fun <ResultT> ReactiveBag<Action<ResultT>>.executeEvery(): Effect<ReactiveBag<ResultT>> = TODO()

fun <ElementT, ResultT> ReactiveBag<ElementT>.executeEveryOf(
    selector: (ElementT) -> Action<ResultT>,
): Effect<ReactiveBag<ResultT>> = map(selector).executeEvery()

fun <ResultT> ReactiveBag<Effect<ResultT>>.actuate(): Effect<ReactiveBag<ResultT>> = TODO()

fun <ElementT, ResultT> ReactiveBag<ElementT>.actuateOf(
    selector: (ElementT) -> Effect<ResultT>,
): Effect<ReactiveBag<ResultT>> = map(selector).actuate()
