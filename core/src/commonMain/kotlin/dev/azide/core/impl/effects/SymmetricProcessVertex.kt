package dev.azide.core.impl.effects

import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

interface SymmetricProcessVertex : ProcessVertex {
    override fun startInternally(
        propagationContext: Transactions.PropagationContext,
    ): Revocable {
        resumeInternally(
            propagationContext = propagationContext,
        )

        return object : Revocable {
            override fun revoke() {
                pauseInternally(
                    propagationContext = propagationContext,
                )
            }
        }
    }

    override fun cancelInternally(
        propagationContext: Transactions.PropagationContext,
    ): Revocable {
        pauseInternally(
            propagationContext = propagationContext,
        )

        return object : Revocable {
            override fun revoke() {
                resumeInternally(
                    propagationContext = propagationContext,
                )
            }
        }
    }

    fun resumeInternally(
        propagationContext: Transactions.PropagationContext,
    )

    fun pauseInternally(
        propagationContext: Transactions.PropagationContext,
    )
}
