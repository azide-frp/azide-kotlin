package dev.azide.core

import dev.azide.core.internal.Transactions

class MomentContext internal constructor(
    internal val propagationContext: Transactions.PropagationContext,
)
