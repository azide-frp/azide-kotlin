package dev.azide.core.collections

import dev.azide.core.Cell
import dev.azide.core.collections.ReactiveCollection.Companion.map
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.ReactiveCollectionSumCellVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.MappedWarmReactiveCollectionVertex

interface ReactiveCollection<out ElementT> {
    companion object {
        fun <ElementT, TransformedElementT> ReactiveCollection<ElementT>.map(
            transform: (ElementT) -> TransformedElementT,
        ): ReactiveCollection<TransformedElementT> = Ordinary(
            vertex = MappedWarmReactiveCollectionVertex(
                sourceVertex = this.vertex,
                transform = transform,
            ),
        )
    }

    class Ordinary<out ElementT> internal constructor(
        override val vertex: ReactiveCollectionVertex<ElementT>,
    ) : ReactiveCollection<ElementT>

    val vertex: ReactiveCollectionVertex<ElementT>
}

val <ElementT> ReactiveCollection<ElementT>.size: Cell<Int>
    get() = Cell.Ordinary(
        vertex = vertex.buildSizeVertex()
    )

fun ReactiveCollection<Int>.sum(): Cell<Int> = Cell.Ordinary(
    ReactiveCollectionSumCellVertex(sourceVertex = this@sum.vertex),
)

fun <ElementT> ReactiveCollection<ElementT>.sumOf(
    selector: (ElementT) -> Int,
): Cell<Int> = map(selector).sum()
