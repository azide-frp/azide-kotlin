package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import kotlin.jvm.JvmInline

abstract class AbstractCachingCellVertex<ValueT> : AbstractSimpleStatelessCellVertex<ValueT>() {
    @JvmInline
    private value class OldValueCache<ValueT>(
        val cachedOldValue: ValueT,
    )

    /**
     * A cache for the old cell's value, maintained as long as the cell is active.
     */
    private var _oldValueCache: OldValueCache<ValueT>? = null

    final override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT {
        when (val oldValueCache = _oldValueCache) {
            null -> {
                val computedOldValue = computeOldValue(propagationContext)

                _oldValueCache = OldValueCache(
                    cachedOldValue = computedOldValue,
                )

                ensureEnqueuedForCommitment(
                    propagationContext = propagationContext,
                )

                return computedOldValue
            }

            else -> {
                return oldValueCache.cachedOldValue
            }
        }
    }

    override fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
        if (ongoingUpdate != null) {
            _oldValueCache = OldValueCache(
                cachedOldValue = ongoingUpdate.updatedValue,
            )
        }
    }

    protected abstract fun computeOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}
