package dev.azide.test_utils.cell

import dev.kmpx.platform.PlatformWeakReference
import dev.azide.Cell
import dev.azide.internal.Transactions
import dev.azide.test_utils.JsGarbageCollectorUtils.awaitCollection

internal object JsCellTestUtils {
    suspend fun <ValueT> ensureCollectible(
        buildCell: () -> Cell<ValueT>,
    ) {
        val (subjectCellWeakRef, subjectVertexWeakRef) = run {
            val subjectCell = buildCell()

            val subjectCellVertex = Transactions.execute { propagationContext ->
                subjectCell.getVertex(propagationContext = propagationContext)
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
