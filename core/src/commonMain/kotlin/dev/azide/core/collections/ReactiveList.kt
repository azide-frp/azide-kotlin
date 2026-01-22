package dev.azide.core.collections

import dev.azide.core.collections.helpers.ReactiveSortableValue
import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex

interface ReactiveList<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: List<ElementT>,
    ) : ReactiveList<ElementT> {
        override val trackedVertex: TrackedCollectionVertex<ElementT>
            get() = TODO("Not yet implemented")
    }

    class Ordinary<out ElementT>
}

val <ElementT> ReactiveList<ElementT>.asReactiveMap: ReactiveMap<Int, ElementT>
    get() = TODO()

fun <ElementT> ReactiveList<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveList<ElementT> = TODO()

fun <ElementT, TransformedElementT> ReactiveList<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ReactiveList<TransformedElementT> = TODO()

fun <ElementT : Comparable<ElementT>> ReactiveCollection<ElementT>.sorted(): ReactiveList<ElementT> =
    TODO()

fun <ElementT, SortKeyT : Comparable<SortKeyT>> ReactiveCollection<SortableValue<ElementT, SortKeyT>>.sorted(): ReactiveList<ElementT> =
    TODO()

fun <ElementT, SortKeyT : Comparable<SortKeyT>> ReactiveBag<ReactiveSortableValue<ElementT, SortKeyT>>.sorted(): ReactiveList<ElementT> =
    TODO()
