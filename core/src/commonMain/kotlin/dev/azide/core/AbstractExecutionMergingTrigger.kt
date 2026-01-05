package dev.azide.core

import dev.azide.core.internal.CommittableVertex
import dev.azide.core.internal.Transactions

abstract class AbstractExecutionMergingTrigger : Trigger, CommittableVertex {
    /**
     * The number of times this trigger has been executed during this transaction.
     */
    private var executionCount = 0

    /**
     * The revocation handle of the implicit inner action executed once per transaction.
     */
    private var storedRevocationHandle: Action.RevocationHandle? = null

    private var isEnqueuedForCommitment = false

    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Triggers.Outcome {
        if (executionCount < 0) {
            throw AssertionError("Unexpected execution count: $executionCount")
        }

        if (executionCount == 0) {
            if (storedRevocationHandle != null) throw AssertionError("Unexpected stored revocation handle")

            storedRevocationHandle = executeInternallyOnce(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )
        }

        if (!isEnqueuedForCommitment) {
            propagationContext.enqueueForCommitment(this)

            isEnqueuedForCommitment = true
        }

        ++executionCount

        return Triggers.Outcomes.of(
            revocationHandle = object : AbstractGuardedRevocationHandle() {
                override fun revokeGuarded() {
                    val storedRevocationHandle =
                        storedRevocationHandle ?: throw AssertionError("No stored revocation handle found")

                    if (executionCount <= 0) {
                        throw AssertionError("Unexpected execution count: $executionCount")
                    }

                    --executionCount

                    if (executionCount == 0) {
                        storedRevocationHandle.revoke()
                        this@AbstractExecutionMergingTrigger.storedRevocationHandle = null
                    }
                }
            },
        )
    }

    abstract fun executeInternallyOnce(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Action.RevocationHandle

    override fun commit() {
        executionCount = 0
    }
}
