package dev.azide.core.internal.effects

import dev.azide.core.internal.Transactions

interface EffectVertex {
    fun start(
        propagationContext: Transactions.PropagationContext,
    )

    fun stop(
        propagationContext: Transactions.PropagationContext,
    )
}
