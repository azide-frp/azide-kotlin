package dev.azide.core.impl.collections.reactive_set

interface FrozenReactiveSetVertex<out ElementT> : ReactiveSetVertex<ElementT> {
    override val ongoingChange: Nothing?
}
