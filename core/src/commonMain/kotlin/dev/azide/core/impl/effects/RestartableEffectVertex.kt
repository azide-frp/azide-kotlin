package dev.azide.core.impl.effects

import dev.azide.core.impl.Transactions

interface RestartableEffectVertex {
    fun start(
        propagationContext: Transactions.PropagationContext,
    )

    fun stop(
        propagationContext: Transactions.PropagationContext,
    )
}
