package dev.azide.core.collections

import dev.azide.core.Cell
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.ReactiveCollectionSumCellVertex

interface ReactiveCollection<out ElementT> {
    val vertex: ReactiveCollectionVertex<ElementT>
}

val <ElementT> ReactiveCollection<ElementT>.size: Cell<Int>
    get() = Cell.Ordinary(
        vertex = vertex.buildSizeVertex()
    )

fun <ElementT, TransformedElementT> ReactiveCollection<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ReactiveCollection<TransformedElementT> = TODO()

fun ReactiveCollection<Int>.sum(): Cell<Int> = Cell.Ordinary(
    ReactiveCollectionSumCellVertex(sourceVertex = this@sum.vertex),
)

fun <ElementT> ReactiveCollection<ElementT>.sumOf(
    selector: (ElementT) -> Int,
): Cell<Int> = map(selector).sum()
