package dev.azide.core.impl.collections.reactive_set.utils

import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex

fun <ElementT> ReactiveSetVertex.SetChange<ElementT>.applyTo(
    mutableSet: MutableSet<ElementT>,
) {
    for (removedElement in this.removedElements) {
        mutableSet.remove(removedElement)
    }

    for (addedElement in this.addedElements) {
        mutableSet.add(addedElement)
    }
}
