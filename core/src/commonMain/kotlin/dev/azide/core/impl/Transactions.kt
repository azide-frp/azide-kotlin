package dev.azide.core.impl

import dev.azide.core.impl.Transactions.PropagationContext.CommitmentCallback
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

    sealed interface ProcessingContext {
        fun enqueueForCommitment(
            committable: Committable,
        ): Revocable
    }

    interface PropagationContext : ProcessingContext {
        typealias CommitmentCallback = () -> Unit

        typealias ExternalExecutionCallback = () -> Unit

        fun enqueueForExecution(
            callback: ExternalExecutionCallback,
        ): Revocable
    }

    interface CommitmentContext : ProcessingContext

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

        val enqueuedCommittables = arrayListOf<Committable?>()

        val callbacksToExecuteExternally = linkedListOf<ExternalExecutionCallback>()

        val propagationContext = object : PropagationContext {

            override fun enqueueForCommitment(
                committable: Committable,
            ): Revocable {
                val predictedIndex = enqueuedCommittables.size

                enqueuedCommittables.add(committable)

                return object : Revocable {
                    override fun revoke() {
                        enqueuedCommittables[predictedIndex] = null
                    }
                }
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

        // ## Commitment phase
        //
        // The part of the transaction when the vertices commit to the new state. The volatile state influences the
        // stable state, after which the volatile state is cleared. Each vertex is committed before the vertices
        // that caused it to update / emit / change.
        enqueuedCommittables.asReversed().forEach { committable ->
            committable?.commit(
                commitmentContext = object : CommitmentContext {
                    override fun enqueueForCommitment(
                        committable: Committable,
                    ): Revocable {
                        // TODO: Figure out why this doesn't make tests fail
                        TODO("Not yet implemented")
                    }
                },
            )
        }

        // ## Side effect execution phase

        callbacksToExecuteExternally.forEach { callback ->
            callback()
        }

        state = TransactionState.Closed

        return result
    }
}

fun Transactions.ProcessingContext.enqueueCallbackForCommitment(
    callback: CommitmentCallback,
) {
    enqueueForCommitment(
        committable = object : Committable {
            override fun commit(
                commitmentContext: Transactions.CommitmentContext,
            ) {
                callback()
            }
        },
    )
}
