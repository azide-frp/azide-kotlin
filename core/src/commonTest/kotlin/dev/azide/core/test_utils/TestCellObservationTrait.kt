package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.registerObserverOnline
import kotlin.test.assertEquals

class TestCellObservationTrait<ValueT> : TestSubjectPerceptionTrait<Cell<ValueT>, TestCellObserver<ValueT>> {
    override fun prepareOldStateStabilityVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectProxy: TestCellObserver<ValueT>,
    ): TestSubjectPerceptionTrait.OldStateStabilityVerifier {
        val originalOldValue = subjectProxy.observedCellVertex.getOldValue(
            propagationContext = propagationContext,
        )

        return object : TestSubjectPerceptionTrait.OldStateStabilityVerifier {
            override fun verifyOldStateDidNotChange() {
                val finalOldValue = subjectProxy.observedCellVertex.getOldValue(
                    propagationContext = propagationContext,
                )

                assertEquals(
                    expected = originalOldValue,
                    actual = finalOldValue,
                    message = "Expected old value view to remain unchanged during propagation.",
                )
            }
        }
    }

    override fun perceive(
        propagationContext: Transactions.PropagationContext,
        subject: Cell<ValueT>,
    ): TestCellObserver<ValueT> {
        val subjectVertex = subject.vertex

        val observer = TestCellObserver(
            observedCellVertex = subjectVertex,
        )

        subjectVertex.registerObserverOnline(
            propagationContext = propagationContext,
            observer = observer,
        )

        return observer
    }
}
