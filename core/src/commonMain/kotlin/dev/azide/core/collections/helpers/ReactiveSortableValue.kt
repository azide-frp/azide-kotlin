package dev.azide.core.collections.helpers

import dev.azide.core.Cell

/**
 * Data class representing a value from a collection that can be sorted with the given key.
 *
 * @property value the underlying value.
 * @property sortKey the key that can be used for sorting.
 */
data class ReactiveSortableValue<ValueT, SortKeyT : Comparable<SortKeyT>>(
    val value: ValueT,
    val sortKey: Cell<SortKeyT>,
)

infix fun <ValueT, SortKeyT : Comparable<SortKeyT>> ValueT.withSortKey(
    sortKey: Cell<SortKeyT>,
): ReactiveSortableValue<ValueT, SortKeyT> = ReactiveSortableValue(
    value = this,
    sortKey = sortKey,
)
