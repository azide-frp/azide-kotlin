package dev.azide.core.collections

import dev.azide.core.Moment
import dev.azide.core.Schedule
import dev.azide.core.collections.helpers.ReactiveSortableValue
import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.PureTrackedListVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedListVertex
import dev.azide.core.impl.collections.reactive_list.operated_vertices.MappedTrackedListVertex
import dev.azide.core.impl.effects.ExternalizedEffect
import dev.azide.core.impl.effects.ReactiveListSyncingSchedule
import kotlin.jvm.JvmName

interface ReactiveList<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: List<ElementT>,
    ) : ReactiveList<ElementT> {
        override val trackedVertex: PureTrackedListVertex<ElementT> = PureTrackedListVertex(constElements)
    }

    class Ordinary<out ElementT>(
        override val trackedVertex: TrackedListVertex<ElementT>,
    ) : ReactiveList<ElementT>

    companion object {
        fun <ElementT> empty(): ReactiveList<ElementT> = Const(emptyList())

        fun <ElementT> of(
            vararg elements: ElementT,
        ): ReactiveList<ElementT> = Const(elements.toList())
    }

    override val trackedVertex: TrackedListVertex<ElementT>
}

val <ElementT> ReactiveList<ElementT>.asReactiveMap: ReactiveMap<Int, ElementT>
    get() = TODO("Unimplemented: asReactiveMap")

val <ElementT> ReactiveList<ElementT>.samplingContent: Moment<List<ElementT>>
    get() = object : Moment<List<ElementT>> {
        override fun pullInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): List<ElementT> = trackedVertex.getOldContentView(
            propagationContext = propagationContext,
        ).toList()
    }

fun <ElementT> ReactiveList<ElementT>.sampleContentExternally(): List<ElementT> = Transactions.executeWithResult { propagationContext ->
    trackedVertex.getOldContentView(
        propagationContext = propagationContext,
    ).toList()
}

fun <ElementT> ReactiveList<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveList<ElementT> = TODO("Unimplemented: filter")

fun <ElementT, TransformedElementT> ReactiveList<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ReactiveList<TransformedElementT> = ReactiveList.Ordinary(
    trackedVertex = MappedTrackedListVertex(
        sourceVertex = this.trackedVertex,
        transform = transform,
    ),
)

fun <ElementT : Comparable<ElementT>> ReactiveCollection<ElementT>.sortedPurely(): ReactiveList<ElementT> =
    TODO("Unimplemented: sortedPurely")

fun <ElementT, SortKeyT : Comparable<SortKeyT>> ReactiveCollection<SortableValue<ElementT, SortKeyT>>.sortedUniquely(): ReactiveList<ElementT> =
    TODO("Unimplemented: sortedUniquely")

fun <ElementT, SortKeyT : Comparable<SortKeyT>> ReactiveBag<ReactiveSortableValue<ElementT, SortKeyT>>.sortedUniquelyReactively(): ReactiveList<ElementT> =
    TODO("Unimplemented: sortedUniquelyReactively")

fun <ElementT> ReactiveList<ElementT>.syncing(
    externalMutableList: MutableList<ElementT>,
): Schedule = ExternalizedEffect(
    internalEffect = ReactiveListSyncingSchedule(
        sourceReactiveList = this@syncing,
        externalMutableList = externalMutableList,
    )
)
