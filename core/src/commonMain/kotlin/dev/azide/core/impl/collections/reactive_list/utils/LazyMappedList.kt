package dev.azide.core.impl.collections.reactive_list.utils

class LazyMappedList<ElementT, TransformedElementT>(
    private val sourceList: List<ElementT>,
    private val transform: (ElementT) -> TransformedElementT,
) : AbstractList<TransformedElementT>() {
    override val size: Int
        get() = sourceList.size

    override fun get(index: Int): TransformedElementT = transform(sourceList[index])
}
