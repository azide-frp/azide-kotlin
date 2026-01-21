package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.registerSetChangeObserver
import kotlin.test.assertEquals

class TestReactiveSetObservationTrait<ElementT> :
    TestSubjectPerceptionTrait<ReactiveSet<ElementT>, TestReactiveSetObserver<ElementT>> {
    override fun prepareOldStateStabilityVerifier(
        propagationContext: Transactions.PropagationContext,
        subject: ReactiveSet<ElementT>,
    ): TestSubjectPerceptionTrait.OldStateStabilityVerifier {
        val originalOldContent = subject.vertex.getOldContentCopy(
            propagationContext = propagationContext,
        )

        return object : TestSubjectPerceptionTrait.OldStateStabilityVerifier {
            override fun verifyOldStateDidNotChange() {
                val finalOldContent = subject.vertex.getOldContentCopy(
                    propagationContext = propagationContext,
                )

                assertEquals(
                    expected = originalOldContent,
                    actual = finalOldContent,
                    message = "Expected old content view to remain unchanged during propagation.",
                )
            }
        }
    }

    override fun perceive(
        propagationContext: Transactions.PropagationContext,
        subject: ReactiveSet<ElementT>,
    ): TestReactiveSetObserver<ElementT> {
        val subjectVertex = subject.vertex

        val observer = TestReactiveSetObserver(
            observedReactiveSetVertex = subjectVertex,
        )

        subjectVertex.registerSetChangeObserver(
            propagationContext = propagationContext,
            observer = observer,
        )

        return observer
    }
}
