package dev.azide.core.impl.collections.reactive_set

interface FrozenTrackedSetVertex<out ElementT> : TrackedSetVertex<ElementT> {
    override val ongoingChange: Nothing?
}
