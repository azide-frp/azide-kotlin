package dev.azide.core.impl.collections.reactive_set.utils

import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex

class NewContentSetView<ElementT>(
    private val oldContentView: Set<ElementT>,
    private val ongoingChange: ReactiveSetVertex.SetChange<ElementT>,
) : AbstractSet<ElementT>() {
    override val size: Int
        get() = oldContentView.size + ongoingChange.sizeDelta

    override fun contains(element: ElementT): Boolean = when {
        ongoingChange.removedElements.contains(element) -> false
        else -> oldContentView.contains(element) || ongoingChange.addedElements.contains(element)
    }

    override fun iterator(): Iterator<ElementT> = asSequence().iterator()

    private fun asSequence(): Sequence<ElementT> = oldContentView.asSequence().filter {
        !ongoingChange.removedElements.contains(it)
    } + ongoingChange.addedElements.asSequence()
}
