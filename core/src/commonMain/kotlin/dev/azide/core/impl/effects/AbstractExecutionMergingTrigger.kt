package dev.azide.core.impl.effects

import dev.azide.core.Trigger
import dev.azide.core.Triggers
import dev.azide.core.impl.AbstractGuardedRevocable
import dev.azide.core.impl.Committable
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

abstract class AbstractExecutionMergingTrigger : Trigger, Committable {
    /**
     * The number of times this trigger has been executed during this transaction.
     */
    private var executionCount = 0

    /**
     * The revocation handle of the implicit inner action executed once per transaction.
     */
    private var storedRevocable: Revocable? = null

    private var isEnqueuedForCommitment = false

    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Triggers.Outcome {
        if (executionCount < 0) {
            throw AssertionError("Unexpected execution count: $executionCount")
        }

        if (executionCount == 0) {
            if (storedRevocable != null) throw AssertionError("Unexpected stored revocation handle")

            storedRevocable = executeInternallyOnce(
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
            revocable = object : AbstractGuardedRevocable() {
                override fun revokeGuarded() {
                    val storedRevocable =
                        storedRevocable ?: throw AssertionError("No stored revocation handle found")

                    if (executionCount <= 0) {
                        throw AssertionError("Unexpected execution count: $executionCount")
                    }

                    --executionCount

                    if (executionCount == 0) {
                        storedRevocable.revoke()
                        this@AbstractExecutionMergingTrigger.storedRevocable = null
                    }
                }
            },
        )
    }

    /**
     * This method is executed only once per transaction, even if the outer trigger is executed multiple times (unless
     * all the outer executions are revoked, in which case the [Revocable] returned by this method is revoked, and a new
     * execution happens on the next outer trigger execution).
     */
    abstract fun executeInternallyOnce(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Revocable

    override fun commit(
        commitmentContext: Transactions.CommitmentContext,
    ) {
        executionCount = 0
    }
}
