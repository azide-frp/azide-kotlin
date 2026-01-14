package dev.azide.core.test_utils.stimulation_test_strategies

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestSubjectPerceptionTrait

/**
 * A stimulation strategy in which the input is stimulated _after_ the test subject is perceived (in the same
 * transaction). Delta propagation is expected.
 */
object PostStimulationTestStrategy : StimulationTestStrategy {
    override fun <SubjectT, SubjectProxyT> verifyStimulationEffectiveness(
        subjectPerceptionTrait: TestSubjectPerceptionTrait<SubjectT, SubjectProxyT>,
        subject: SubjectT,
        inputStimulation: TestInputStimulation,
        expectedSubjectReaction: ExpectedTestSubjectReaction<SubjectT, SubjectProxyT>,
        expectedTargetImpact: ExpectedTestTargetImpact?,
    ) {
        val targetImpactVerifier = expectedTargetImpact?.prepareImpactVerifier()

        val newStateVerifier = Transactions.executeWithResult { propagationContext ->
            val subjectProxy = subjectPerceptionTrait.perceive(
                propagationContext = propagationContext,
                subject = subject,
            )

            val oldStateStabilityVerifier = subjectPerceptionTrait.prepareOldStateStabilityVerifier(
                propagationContext = propagationContext,
                subject = subject,
            )

            val newStateVerifier = expectedSubjectReaction.prepareNewStateVerifier(
                propagationContext = propagationContext,
                subject = subject,
            )

            val deltaVerifier = expectedSubjectReaction.prepareDeltaVerifier(
                propagationContext = propagationContext,
                subjectProxy = subjectProxy,
            )

            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )

            deltaVerifier.verifyExposedCorrectly()

            deltaVerifier.verifyPropagatedCorrectly()

            oldStateStabilityVerifier.verifyOldStateDidNotChange()

            newStateVerifier
        }

        targetImpactVerifier?.verifyPostTransaction()

        Transactions.execute { propagationContext ->
            newStateVerifier.verifyNewState(
                propagationContext = propagationContext,
            )
        }
    }
}
