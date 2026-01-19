package dev.azide.core

import dev.azide.core.impl.Transactions

class MomentContextImpl internal constructor(
    override val propagationContext: Transactions.PropagationContext,
    override val wrapUpContext: Transactions.WrapUpContext,
) : MomentContext
