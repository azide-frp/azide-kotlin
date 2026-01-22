package dev.azide.core.impl.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.effects.EffectVertex

class ReactiveListSyncingScheduleVertex<ElementT> private constructor(
    propagationContext: Transactions.PropagationContext,
    private val sourceReactiveList: ReactiveList<ElementT>,
    private val externalMutableList: MutableList<ElementT>,
) : Revocable, EffectVertex, CommittableVertex {
    companion object {
        fun <ElementT> startInternally(
            propagationContext: Transactions.PropagationContext,
            sourceReactiveList: ReactiveList<ElementT>,
            externalMutableList: MutableList<ElementT>,
        ): ReactiveListSyncingScheduleVertex<ElementT> = ReactiveListSyncingScheduleVertex(
            propagationContext = propagationContext,
            sourceReactiveList = sourceReactiveList,
            externalMutableList = externalMutableList,
        )
    }

    override fun cancelInternally(
        propagationContext: Transactions.PropagationContext,
    ): Revocable {
        TODO()
    }

    override fun commit() {
        TODO()
    }

    override fun revoke() {

        TODO()
    }

    init {
        propagationContext.enqueueForCommitment(this)
    }
}
