package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import kotlin.jvm.JvmInline

abstract class AbstractCachingCellVertex<ValueT>(
    private val cacheType: CacheType,
) : AbstractStatelessCellVertex<ValueT>() {
    enum class CacheType {
        Momentary, Active,
    }

    @JvmInline
    private value class OldValueCache<ValueT>(
        val cachedOldValue: ValueT,
    )

    /**
     * A cache for the old cell's value. Depending on the [cacheType], it may be maintained only for the duration of a
     * single transaction, or as long as the cell is active.
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
        when (cacheType) {
            CacheType.Momentary -> {
                _oldValueCache = null
            }

            CacheType.Active -> {
                if (ongoingUpdate != null) {
                    _oldValueCache = OldValueCache(
                        cachedOldValue = ongoingUpdate.updatedValue,
                    )
                }
            }
        }
    }

    protected abstract fun computeOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}
