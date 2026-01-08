package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

interface TransactionTestContext {
    val propagationContext: Transactions.PropagationContext
}
