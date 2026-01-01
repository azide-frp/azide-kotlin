package dev.azide.core.test_utils.cell

import dev.kmpx.platform.PlatformWeakReference
import dev.azide.core.Cell
import dev.azide.core.internal.Transactions
import dev.azide.core.test_utils.JsGarbageCollectorUtils.awaitCollection

internal object JsCellTestUtils {
    suspend fun <ValueT> ensureCollectible(
        buildCell: () -> Cell<ValueT>,
    ) {
        val (subjectCellWeakRef, subjectVertexWeakRef) = run {
            val subjectCell = buildCell()

            val subjectCellVertex = Transactions.executeWithResult { propagationContext ->
                subjectCell.vertex
            }

            Pair(
                PlatformWeakReference(subjectCell),
                PlatformWeakReference(subjectCellVertex),
            )
        }

        subjectCellWeakRef.awaitCollection()

        subjectVertexWeakRef.awaitCollection()
    }
}
