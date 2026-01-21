package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.registerBoundUpdateObserverOnline
import kotlin.test.assertEquals

class TestCellObservationTrait<ValueT> : TestSubjectPerceptionTrait<Cell<ValueT>, TestCellObserver<ValueT>> {
    override fun prepareOldStateStabilityVerifier(
        propagationContext: Transactions.PropagationContext,
        subject: Cell<ValueT>,
    ): TestSubjectPerceptionTrait.OldStateStabilityVerifier {
        val originalOldValue = subject.vertex.getOldValue(
            propagationContext = propagationContext,
        )

        return object : TestSubjectPerceptionTrait.OldStateStabilityVerifier {
            override fun verifyOldStateDidNotChange() {
                val finalOldValue = subject.vertex.getOldValue(
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

        subjectVertex.registerBoundUpdateObserverOnline(
            propagationContext = propagationContext,
            observer = observer,
        )

        return observer
    }

    fun viaEffectOutcome(): TestSubjectPerceptionTrait<Effect.Outcome<Cell<ValueT>>, TestCellObserver<ValueT>> =
        object : TestSubjectPerceptionTrait<Effect.Outcome<Cell<ValueT>>, TestCellObserver<ValueT>> {
            override fun prepareOldStateStabilityVerifier(
                propagationContext: Transactions.PropagationContext,
                subject: Effect.Outcome<Cell<ValueT>>,
            ): TestSubjectPerceptionTrait.OldStateStabilityVerifier =
                this@TestCellObservationTrait.prepareOldStateStabilityVerifier(
                    propagationContext = propagationContext,
                    subject = subject.result,
                )

            override fun perceive(
                propagationContext: Transactions.PropagationContext,
                subject: Effect.Outcome<Cell<ValueT>>,
            ): TestCellObserver<ValueT> = this@TestCellObservationTrait.perceive(
                propagationContext = propagationContext,
                subject = subject.result,
            )
        }
}
