package dev.azide.core.impl

import dev.azide.core.MomentContext

class MomentContextImpl internal constructor(
    override val propagationContext: Transactions.PropagationContext,
    override val wrapUpContext: Transactions.WrapUpContext,
) : MomentContext
