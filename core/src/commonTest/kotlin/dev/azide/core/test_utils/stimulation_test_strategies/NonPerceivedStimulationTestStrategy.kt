package dev.azide.core.test_utils.stimulation_test_strategies

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestSubjectPerceptionTrait

/**
 * A stimulation strategy in which the test subject is not perceived at all. This strategy should not be used with
 * test cases in which there's no possible target impact (i.e. pure reactive operations).
 */
object NonPerceivedStimulationTestStrategy : StimulationTestStrategy {
    override fun <SubjectT, SubjectProxyT> verifyStimulationEffectiveness(
        subjectPerceptionTrait: TestSubjectPerceptionTrait<SubjectT, SubjectProxyT>,
        subject: SubjectT,
        inputStimulation: TestInputStimulation,
        expectedSubjectReaction: ExpectedTestSubjectReaction<SubjectProxyT>,
        expectedTargetImpact: ExpectedTestTargetImpact?,
    ) {
        if (expectedTargetImpact == null) {
            throw IllegalArgumentException("Non-perceived stimulation strategy requires expected target impact to be provided.")
        }

        val targetImpactVerifier = expectedTargetImpact.prepareImpactVerifier()

        Transactions.executeWithResult { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }

        targetImpactVerifier.verifyPostTransaction()
    }
}
