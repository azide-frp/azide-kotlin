package dev.azide.core.impl.collections.reactive_collection

interface FrozenTrackedGenericCollectionVertex<out ContentT : Collection<*>> : TrackedGenericCollectionVertex<ContentT, Nothing> {
    override val ongoingChange: Nothing?
}
