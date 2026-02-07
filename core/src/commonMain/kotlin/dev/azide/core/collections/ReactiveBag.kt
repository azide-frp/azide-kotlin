package dev.azide.core.collections

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.Moment
import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_collection.PureTrackedBagVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_collection.buildContainsVertex
import dev.azide.core.impl.effects.AbstractExternalizedEffect
import dev.azide.core.impl.collections.reactive_bag.operated_vertices.ActuatedTaggedBagVertex
import dev.azide.core.pullExternally

interface ReactiveBag<out ElementT> : ReactiveCollection<ElementT> {
    typealias Tag = Any?

    class Const<out ElementT>(
        constElements: TaggedBag<ElementT>,
    ) : ReactiveBag<ElementT> {
        override val trackedVertex: TrackedTaggedBagVertex<ElementT> = PureTrackedBagVertex(
            elements = constElements,
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val trackedVertex: TrackedTaggedBagVertex<ElementT>,
    ) : ReactiveBag<ElementT>

    override val trackedVertex: TrackedTaggedBagVertex<ElementT>
}

val <ElementT> ReactiveBag<ElementT>.samplingTaggedContent: Moment<Map<Tag, ElementT>>
    get() = object : Moment<Map<Tag, ElementT>> {
        override fun pullInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Map<Tag, ElementT> = trackedVertex.getOldContentView(
            propagationContext = propagationContext,
        ).elementByTag.toMap()
    }

fun <ElementT> ReactiveBag<ElementT>.sampleTaggedContentExternally(): Map<Tag, ElementT> =
    samplingTaggedContent.pullExternally()

fun <ElementT> ReactiveBag<ElementT>.contains(
    element: ElementT,
): Cell<Boolean> = Cell.Ordinary(
    vertex = trackedVertex.buildContainsVertex(element = element),
)

fun <ElementT> ReactiveBag<ElementT>.filter(
    predicate: (ElementT) -> Boolean,
): ReactiveBag<ElementT> = TODO()

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

fun <InnerResultT> ReactiveBag<Effect<InnerResultT>>.actuate(): Effect<ReactiveBag<InnerResultT>> =
    object : AbstractExternalizedEffect<ReactiveBag<InnerResultT>>(
        internalEffect = ActuatedTaggedBagVertex.ActuationEffect(
            sourceEffectBag = this@actuate,
        ),
    ) {}

fun <ElementT, ResultT> ReactiveBag<ElementT>.actuateOf(
    selector: (ElementT) -> Effect<ResultT>,
): Effect<ReactiveBag<ResultT>> = map(selector).actuate()
