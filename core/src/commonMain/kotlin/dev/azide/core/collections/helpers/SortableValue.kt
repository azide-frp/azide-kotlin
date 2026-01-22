package dev.azide.core.collections.helpers

/**
 * Data class representing a value from a collection that can be sorted with the given key.
 *
 * @property value the underlying value.
 * @property sortKey the key that can be used for sorting.
 */
data class SortableValue<ValueT, SortKeyT : Comparable<SortKeyT>>(
    val value: ValueT,
    val sortKey: SortKeyT,
)

infix fun <ValueT, SortKeyT : Comparable<SortKeyT>> ValueT.withSortKey(
    sortKey: SortKeyT,
): SortableValue<ValueT, SortKeyT> = SortableValue(
    value = this,
    sortKey = sortKey,
)
