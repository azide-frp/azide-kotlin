package dev.azide.core.impl

interface Committable {
    fun commit(
        commitmentContext: Transactions.CommitmentContext,
    )
}
