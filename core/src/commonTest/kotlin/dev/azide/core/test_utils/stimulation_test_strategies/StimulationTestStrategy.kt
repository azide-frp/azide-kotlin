package dev.azide.core.test_utils.stimulation_test_strategies

import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestSubjectPerceptionTrait

interface StimulationTestStrategy {
    fun <SubjectT, SubjectProxyT> verifyStimulationEffectiveness(
        subjectPerceptionTrait: TestSubjectPerceptionTrait<SubjectT, SubjectProxyT>,
        subject: SubjectT,
        inputStimulation: TestInputStimulation,
        expectedSubjectReaction: ExpectedTestSubjectReaction<SubjectT, SubjectProxyT>,
        expectedTargetImpact: ExpectedTestTargetImpact? = null,
    )
}
