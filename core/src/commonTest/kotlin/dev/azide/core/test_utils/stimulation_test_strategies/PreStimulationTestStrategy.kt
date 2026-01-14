package dev.azide.core.test_utils.stimulation_test_strategies

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestSubjectPerceptionTrait

/**
 * A stimulation strategy in which the input is stimulated _before_ the test subject is perceived. Delta propagation is
 * unexpected.
 */
object PreStimulationTestStrategy : StimulationTestStrategy {
    override fun <SubjectT, SubjectProxyT> verifyStimulationEffectiveness(
        subjectPerceptionTrait: TestSubjectPerceptionTrait<SubjectT, SubjectProxyT>,
        subject: SubjectT,
        inputStimulation: TestInputStimulation,
        expectedSubjectReaction: ExpectedTestSubjectReaction<SubjectT, SubjectProxyT>,
        expectedTargetImpact: ExpectedTestTargetImpact?,
    ) {
        val targetImpactVerifier = expectedTargetImpact?.prepareImpactVerifier()

        val newStateVerifier = Transactions.executeWithResult { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )

            val subjectProxy = subjectPerceptionTrait.perceive(
                propagationContext = propagationContext,
                subject = subject,
            )

            expectedSubjectReaction.prepareDeltaVerifier(
                propagationContext = propagationContext,
                subjectProxy = subjectProxy,
            ).verifyExposedCorrectly()

            val newStateVerifier = expectedSubjectReaction.prepareNewStateVerifier(
                propagationContext = propagationContext,
                subject = subject,
            )

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
