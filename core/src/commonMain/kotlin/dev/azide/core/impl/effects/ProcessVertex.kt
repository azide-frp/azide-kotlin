package dev.azide.core.impl.effects

import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

/**
 * An internal low-level effect-related vertex.
 */
interface ProcessVertex {
    /**
     * Start the effect internally.
     *
     * @return A [Revocable] that can be used to revoke the start operation. After the revocation, the instance must
     * be cleared and ready to be re-used (as if [startInternally] was never called).
     */
    fun startInternally(
        propagationContext: Transactions.PropagationContext,
    ): Revocable

    /**
     * Cancel the effect internally. This method will only be called in the transactions following the one in which
     * the effect was started. In the initial transaction, the effect will be revoked using the [Revocable] returned
     * by [startInternally] instead.
     *
     * @return A [Revocable] that can be used to revoke the cancel operation. After the revocation, the instance must
     * be fully operational (as if [cancelInternally] wasn't called).
     */
    fun cancelInternally(
        propagationContext: Transactions.PropagationContext,
    ): Revocable
}
