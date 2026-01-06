package dev.azide.core.test_utils

import dev.azide.core.internal.Transactions

interface TransactionTestContext {
    val propagationContext: Transactions.PropagationContext
}
