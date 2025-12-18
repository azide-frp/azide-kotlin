package dev.azide.core

import dev.azide.core.internal.Transactions

interface MomentContext {
    val propagationContext: Transactions.PropagationContext
}
