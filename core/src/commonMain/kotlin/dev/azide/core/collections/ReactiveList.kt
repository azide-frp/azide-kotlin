package dev.azide.core.collections

import dev.azide.core.Schedule
import dev.azide.core.collections.helpers.ReactiveSortableValue
import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.effects.AbstractPrimitiveSchedule
import dev.azide.core.impl.effects.AdaptedExternalScheduleVertex
import kotlin.jvm.JvmName

interface ReactiveList<out ElementT> : ReactiveCollection<ElementT> {
    class Const<out ElementT>(
        constElements: List<ElementT>,
    ) : ReactiveList<ElementT> {
        override val trackedVertex: TrackedCollectionVertex<ElementT>
            get() = TODO("Not yet implemented")
    }

    class Ordinary<out ElementT>

    companion object {
        fun <ElementT> empty(): ReactiveList<ElementT> = Const(emptyList())

        fun <ElementT> of(
            vararg elements: ElementT,
        ): ReactiveList<ElementT> = Const(elements.toList())
    }
}

val <ElementT> ReactiveList<ElementT>.asReactiveMap: ReactiveMap<Int, ElementT>
    get() = TODO()

fun <ElementT> ReactiveList<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveList<ElementT> = TODO()

fun <ElementT, TransformedElementT> ReactiveList<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ReactiveList<TransformedElementT> = TODO()

fun <ElementT : Comparable<ElementT>> ReactiveCollection<ElementT>.sorted(): ReactiveList<ElementT> = TODO()

@JvmName("sortedSortableValue")
fun <ElementT, SortKeyT : Comparable<SortKeyT>> ReactiveCollection<SortableValue<ElementT, SortKeyT>>.sorted(): ReactiveList<ElementT> =
    TODO()

@JvmName("sortedReactiveSortableValue")
fun <ElementT, SortKeyT : Comparable<SortKeyT>> ReactiveBag<ReactiveSortableValue<ElementT, SortKeyT>>.sorted(): ReactiveList<ElementT> =
    TODO()

fun <ElementT> ReactiveList<ElementT>.syncing(
    externalMutableList: MutableList<ElementT>,
): Schedule = object : AbstractPrimitiveSchedule<AdaptedExternalScheduleVertex>() {
    override fun startInternally(
        propagationContext: PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): AdaptedExternalScheduleVertex = TODO()
}
