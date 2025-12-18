package dev.azide.core.internal

abstract class Transaction<out ResultT> {
    abstract fun propagate(
        propagationContext: Transactions.PropagationContext,
    ): ResultT

    fun execute(): ResultT = Transactions.execute {
        propagate(it)
    }
}
