package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.CommittableVertex
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import kotlin.jvm.JvmInline

abstract class AbstractDerivedFrozenCellVertex<ValueT> : AbstractFrozenCellVertex<ValueT>(), CommittableVertex {
    @JvmInline
    private value class FrozenValueCache<ValueT>(
        val cachedOldValue: ValueT,
    )

    /**
     * A cache for the frozen cell's value. It is maintained only for the duration of a single transaction.
     */
    private var _frozenValueCache: FrozenValueCache<ValueT>? = null

    final override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT {
        when (val frozenValueCache = _frozenValueCache) {
            null -> {
                val computedOldValue = computeFrozenValue(propagationContext)

                _frozenValueCache = FrozenValueCache(
                    cachedOldValue = computedOldValue,
                )

                propagationContext.enqueueForCommitment(this)

                return computedOldValue
            }

            else -> {
                return frozenValueCache.cachedOldValue
            }
        }
    }

    final override fun commit() {
        _frozenValueCache = null
    }

    protected abstract fun computeFrozenValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}
