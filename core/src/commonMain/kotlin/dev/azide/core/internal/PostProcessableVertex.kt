package dev.azide.core.internal

interface PostProcessableVertex {
    /**
     * Post-process the vertex after all events were already propagated, but before vertices have commited to the new
     * state. Post-processing operations should not enqueue for further post-processing.
     */
    fun postProcess(
        propagationContext: Transactions.PropagationContext,
    )
}
