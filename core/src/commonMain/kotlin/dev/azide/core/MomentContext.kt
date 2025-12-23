package dev.azide.core

import dev.azide.core.internal.Transactions

interface MomentContext {
    companion object {
        fun wrap(
            propagationContext: Transactions.PropagationContext,
        ): MomentContext = MomentContextImpl(
            propagationContext = propagationContext,
        )
    }

    val propagationContext: Transactions.PropagationContext
}
