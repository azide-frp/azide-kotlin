package dev.azide.core.impl.collections.reactive_set.utils

class LazyFilteredSet<ElementT>(
    private val sourceSet: Set<ElementT>,
    private val predicate: (ElementT) -> Boolean,
) : AbstractSet<ElementT>() {
    override val size: Int
        get() = sourceSet.count(predicate)

    override fun contains(element: ElementT): Boolean = when {
        !predicate(element) -> false
        else -> sourceSet.contains(element)
    }

    override fun iterator(): Iterator<ElementT> = asSequence().iterator()

    private fun asSequence(): Sequence<ElementT> = sourceSet.asSequence().filter(predicate)
}
