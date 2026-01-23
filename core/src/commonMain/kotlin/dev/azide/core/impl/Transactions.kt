package dev.azide.core.impl

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Transactions.PropagationContext.ExternalExecutionCallback
import dev.kmpx.collections.lists.linkedListOf

object Transactions {
    interface WrapUpContext {
        companion object {
            fun <ResultT> wrapUp(
                propagationContext: PropagationContext,
                block: (WrapUpContext) -> ResultT,
            ): ResultT {
                val callbacks = mutableListOf<WrapUpCallback>()

                val wrapUpContext = object : WrapUpContext {
                    override fun enqueueForWrapUp(
                        callback: WrapUpCallback,
                    ) {
                        callbacks.add(callback)
                    }
                }

                val result = block(wrapUpContext)

                callbacks.forEach { callback ->
                    callback(propagationContext)
                }

                return result
            }
        }

        typealias WrapUpCallback = (PropagationContext) -> Unit

        fun enqueueForWrapUp(
            callback: WrapUpCallback,
        )
    }

    interface PropagationContext {
        typealias PostProcessingCallback = () -> Unit

        typealias CommitmentCallback = () -> Unit

        typealias ExternalExecutionCallback = () -> Unit

        fun enqueueCallbackForPostProcessing(
            callback: PostProcessingCallback,
        ): Revocable

        fun enqueueCallbackForCommitment(
            callback: CommitmentCallback,
        )

        fun enqueueForExecution(
            callback: ExternalExecutionCallback,
        ): Revocable
    }

    enum class TransactionState {
        Open, Closed,
    }

    fun execute(
        propagate: (PropagationContext) -> Unit,
    ) {
        executeWithResult(
            propagate = propagate,
        )
    }

    fun <ResultT> executeWithResult(
        propagate: (PropagationContext) -> ResultT,
    ): ResultT {
        var state = TransactionState.Open

        fun ensureIsOpen() {
            if (state != TransactionState.Open) {
                throw IllegalStateException("Transaction is already closed")
            }
        }

        val enqueuedPostProcessingCallbacks = linkedListOf<PropagationContext.PostProcessingCallback>()

        val enqueuedCommitmentCallbacks = arrayListOf<PropagationContext.CommitmentCallback>()

        val callbacksToExecuteExternally = linkedListOf<ExternalExecutionCallback>()

        val propagationContext = object : PropagationContext {
            override fun enqueueCallbackForPostProcessing(
                callback: PropagationContext.PostProcessingCallback,
            ): Revocable {
                ensureIsOpen()

                val innerHandle = enqueuedPostProcessingCallbacks.append(callback)

                return object : Revocable {
                    override fun revoke() {
                        ensureIsOpen()

                        enqueuedPostProcessingCallbacks.removeVia(innerHandle)
                    }
                }
            }

            override fun enqueueCallbackForCommitment(
                callback: PropagationContext.CommitmentCallback,
            ) {
                ensureIsOpen()

                enqueuedCommitmentCallbacks.add(callback)
            }

            override fun enqueueForExecution(
                callback: ExternalExecutionCallback,
            ): Revocable {
                ensureIsOpen()

                val innerHandle = callbacksToExecuteExternally.append(callback)

                return object : Revocable {
                    override fun revoke() {
                        ensureIsOpen()

                        callbacksToExecuteExternally.removeVia(innerHandle)
                    }
                }
            }
        }

        // ## Propagation phase
        //
        // The main phase of the transaction, when information flows through the vertex graph. The emissions, updates,
        // and changes are propagated, actions are executed, etc. During the propagation, vertices may enqueue for
        // post-processing, commitment and side effect execution.

        val result = propagate(
            propagationContext,
        )

        // ## Post-processing phase
        //
        // The part of the transaction after all information was propagated, but vertices haven't yet commited to the
        // new state. Some vertices can adjust their subscriptions / observations for the sake of future transactions.
        // Enqueueing for commitment is still possible. Enqueueing for further post-processing is prohibited. Enqueueing
        // side effects is not expected or needed.

        enqueuedPostProcessingCallbacks.forEach { callback ->
            callback()
        }

        // ## Commitment phase
        //
        // The part of the transaction when the vertices commit to the new state. The volatile state influences the
        // stable state, after which the volatile state is cleared. Accessing other vertices' state (stable or volatile)
        // is prohibited, as it's undefined whether they are before or after their own commitment. Removing listeners
        // / listeners and (in consequence) deactivation is allowed.
        // TODO: Figure out if upstream unregistration (e.g. of `EventStream.single`) shouldn't be moved to
        //  post-processing for consistency

        enqueuedCommitmentCallbacks.forEach { callback ->
            callback()
        }

        // ## Side effect execution phase

        callbacksToExecuteExternally.forEach { callback ->
            callback()
        }

        state = TransactionState.Closed

        return result
    }
}

fun PropagationContext.enqueueForPostProcessing(
    vertex: PostProcessableVertex,
) {
    this.enqueueCallbackForPostProcessing {
        vertex.postProcess(
            propagationContext = this,
        )
    }
}

fun PropagationContext.enqueueForCommitment(
    vertex: CommittableVertex,
) {
    this.enqueueCallbackForCommitment {
        vertex.commit()
    }
}
