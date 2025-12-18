package dev.azide

import dev.azide.internal.Transactions

class MomentContext internal constructor(
    internal val propagationContext: Transactions.PropagationContext,
)
