package dev.azide.core

import dev.azide.core.internal.Transactions

class MomentContextImpl internal constructor(
    override val propagationContext: Transactions.PropagationContext,
    override val wrapUpContext: Transactions.WrapUpContext,
) : MomentContext
