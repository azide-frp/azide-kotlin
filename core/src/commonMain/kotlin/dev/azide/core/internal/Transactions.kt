package dev.azide.core.internal

import dev.azide.core.Action
import dev.azide.core.ExternalSideEffect
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
        fun enqueueForCommitment(
            vertex: CommittableVertex,
        )

        fun enqueueForExecution(
            sideEffect: ExternalSideEffect,
        ): Action.RevocationHandle
    }

    inline fun execute(
        propagate: (PropagationContext) -> Unit,
    ) {
        executeWithResult(
            propagate = propagate,
        )
    }

    inline fun <ResultT> executeWithResult(
        propagate: (PropagationContext) -> ResultT,
    ): ResultT {
        val verticesToCommit = arrayListOf<CommittableVertex>()

        val sideEffectsToExecute = linkedListOf<ExternalSideEffect>()

        val result = propagate(
            object : PropagationContext {
                override fun enqueueForCommitment(
                    vertex: CommittableVertex,
                ) {
                    verticesToCommit.add(vertex)
                }

                override fun enqueueForExecution(
                    sideEffect: ExternalSideEffect,
                ): Action.RevocationHandle {
                    val innerHandle = sideEffectsToExecute.append(sideEffect)

                    return object : Action.RevocationHandle {
                        override fun revoke() {
                            sideEffectsToExecute.removeVia(innerHandle)
                        }
                    }
                }
            },
        )

        verticesToCommit.forEach { vertex ->
            vertex.commit()
        }

        sideEffectsToExecute.forEach { sideEffect ->
            sideEffect.executeExternally()
        }

        return result
    }
}
