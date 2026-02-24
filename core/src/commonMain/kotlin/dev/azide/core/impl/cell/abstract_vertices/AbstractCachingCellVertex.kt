package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import kotlin.jvm.JvmInline

abstract class AbstractCachingCellVertex<ValueT> : AbstractStatelessCellVertex<ValueT>() {
    @JvmInline
    private value class OldValueCache<ValueT>(
        val cachedOldValue: ValueT,
    )

    /**
     * A cache for the old cell's value, maintained as long as the cell is active.
     */
    private var _oldValueCache: OldValueCache<ValueT>? = null

    final override fun prepare(processingContext: Transactions.ProcessingContext) {
        _oldValueCache = OldValueCache(
            cachedOldValue = computeOldValue(processingContext = processingContext),
        )
    }

    final override fun reset() {
        _oldValueCache = null
    }

    final override fun getOldValue(
        processingContext: Transactions.ProcessingContext,
    ): ValueT = when (val oldValueCache = _oldValueCache) {
        // The cell seems to be inactive, compute the value on demand
        null -> computeOldValue(processingContext = processingContext)

        // The cell seems to be active, return the maintained cached value
        else -> oldValueCache.cachedOldValue
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
        processingContext: Transactions.ProcessingContext,
    ): ValueT
}
