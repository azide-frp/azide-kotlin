package dev.azide.core.impl

import dev.kmpx.collections.lists.linkedListOf
import dev.azide.core.impl.Transactions.PropagationContext.ExternalExecutionCallback

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
        typealias ExternalExecutionCallback = () -> Unit

        fun enqueueForPostProcessing(
            vertex: PostProcessableVertex,
        )

        fun enqueueForCommitment(
            vertex: CommittableVertex,
        )

        fun enqueueForExecution(
            callback: ExternalExecutionCallback,
        ): Revocable
    }

    enum class TransactionState {
        Open,
        Closed,
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

        val verticesToPostProcess = arrayListOf<PostProcessableVertex>()

        val verticesToCommit = arrayListOf<CommittableVertex>()

        val callbacksToExecuteExternally = linkedListOf<ExternalExecutionCallback>()

        val propagationContext = object : PropagationContext {
            override fun enqueueForPostProcessing(
                vertex: PostProcessableVertex,
            ) {
                ensureIsOpen()

                verticesToPostProcess.add(vertex)
            }

            override fun enqueueForCommitment(
                vertex: CommittableVertex,
            ) {
                ensureIsOpen()

                verticesToCommit.add(vertex)
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

        verticesToPostProcess.forEach { vertex ->
            vertex.postProcess(
                propagationContext = propagationContext,
            )
        }

        // ## Commitment phase
        //
        // The part of the transaction when the vertices commit to the new state. The volatile state influences the
        // stable state, after which the volatile state is cleared. Accessing other vertices' state (stable or volatile)
        // is prohibited, as it's undefined whether they are before or after their own commitment. Removing listeners
        // / observers and (in consequence) deactivation is allowed.
        // TODO: Figure out if upstream unregistration (e.g. of `EventStream.single`) shouldn't be moved to
        //  post-processing for consistency

        verticesToCommit.forEach { vertex ->
            vertex.commit()
        }

        // ## Side effect execution phase

        callbacksToExecuteExternally.forEach { callback ->
            callback()
        }

        state = TransactionState.Closed

        return result
    }
}
