package dev.azide.core.impl.effects

import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

interface InternalEffect<SubjectT : InternalEffect.Subject> {
    interface Subject {
        fun cancelInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Revocable
    }

    interface RevocableOutcome<SubjectT : InternalEffect.Subject> : Revocable {
        val subject: SubjectT
    }

    fun startInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): RevocableOutcome<SubjectT>
}
