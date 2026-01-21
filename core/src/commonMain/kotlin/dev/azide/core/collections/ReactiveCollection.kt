package dev.azide.core.collections

import dev.azide.core.Cell
import dev.azide.core.collections.ReactiveCollection.Companion.map
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.TrackedCollectionSumCellVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.MappedWarmTrackedCollectionVertex

interface ReactiveCollection<out ElementT> {
    companion object {
        fun <ElementT, TransformedElementT> ReactiveCollection<ElementT>.map(
            transform: (ElementT) -> TransformedElementT,
        ): ReactiveCollection<TransformedElementT> = Ordinary(
            vertex = MappedWarmTrackedCollectionVertex(
                sourceVertex = this.vertex,
                transform = transform,
            ),
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val vertex: TrackedCollectionVertex<ElementT>,
    ) : ReactiveCollection<ElementT>

    val vertex: TrackedCollectionVertex<ElementT>
}

val <ElementT> ReactiveCollection<ElementT>.size: Cell<Int>
    get() = Cell.Ordinary(
        vertex = vertex.buildSizeVertex()
    )

fun ReactiveCollection<Int>.sum(): Cell<Int> = Cell.Ordinary(
    TrackedCollectionSumCellVertex(sourceVertex = this@sum.vertex),
)

fun <ElementT> ReactiveCollection<ElementT>.sumOf(
    selector: (ElementT) -> Int,
): Cell<Int> = map(selector).sum()
