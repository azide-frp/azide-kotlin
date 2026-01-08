package dev.azide.core.impl.effects

import dev.azide.core.impl.RevocationHandle
import dev.azide.core.impl.Transactions

interface EffectVertex {
    fun cancelInternally(
        propagationContext: Transactions.PropagationContext,
    ): RevocationHandle
}
