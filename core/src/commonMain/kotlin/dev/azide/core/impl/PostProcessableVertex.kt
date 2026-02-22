package dev.azide.core.impl

// TODO: Merge with `Committable`
interface PostProcessableVertex {
    /**
     * Post-process the vertex after all events were already propagated, but before vertices have commited to the new
     * state. Post-processing operations should not enqueue for further post-processing.
     */
    fun postProcess(
        propagationContext: Transactions.PropagationContext,
    )
}
