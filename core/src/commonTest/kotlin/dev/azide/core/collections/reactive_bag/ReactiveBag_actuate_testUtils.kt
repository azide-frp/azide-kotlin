package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.sampleTaggedContentExternally
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_step_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveBag_actuate_testUtils {
    data object SourceEffectReactiveBagTag : TestInputReactiveCollectionTag

    enum class TargetEffectTag {
        TargetEffect1, TargetEffect2, TargetEffect3, TargetEffect4, TargetEffect5, TargetEffect6, TargetEffect7, ExtraTargetEffect,
    }

    val stimulationBank_sourceEffectBagChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveBagTag,
        ),
    )

    val stimulationBank_sourceEffectBagChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveBagTag,
        ),
    )

    val stimulationBank_sourceEffectBagChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveBagTag,
        ),
    )

    fun verifyEffectNotOngoing(
        sourceReactiveBag: TestInputReactiveBag<TestTargetEffect<Int>>,
        subjectReactiveBag: ReactiveBag<Int>,
    ) {
        val preStimulationTaggedContent = subjectReactiveBag.sampleTaggedContentExternally()

        val extraTargetEffect = TestTargetEffect.pure(result = -1)

        Effect_ReactiveBag_step_testUtils.executeStepTransaction(
            subjectReactiveBag = subjectReactiveBag,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.change(
                description = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.ExtraTargetEffect to extraTargetEffect,
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = preStimulationTaggedContent,
            ),
            expectedTargetImpact = extraTargetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceReactiveBag: TestInputReactiveBag<TestTargetEffect<Int>>,
    ) {
        val extraTargetEffect = TestTargetEffect.pure(result = -1)

        Effect_generic_step_testUtils.executeStepTransaction(
            subject = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceReactiveBag.change(
                description = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.ExtraTargetEffect to extraTargetEffect,
                    ),
                ),
            ),
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = extraTargetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectOngoing(
        sourceReactiveBag: TestInputReactiveBag<TestTargetEffect<Int>>,
        subjectReactiveBag: ReactiveBag<Int>,
    ) {
        val preStimulationContent = subjectReactiveBag.sampleTaggedContentExternally()

        val extraTargetEffect = TestTargetEffect.pure(result = 0)

        Effect_ReactiveBag_step_testUtils.executeStepTransaction(
            subjectReactiveBag = subjectReactiveBag,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.change(
                description = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.ExtraTargetEffect to extraTargetEffect,
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedContent = preStimulationContent,
                expectedNewTaggedContent = preStimulationContent + (TargetEffectTag.ExtraTargetEffect to 0),
            ),
            expectedTargetImpact = extraTargetEffect.expectIsStartedOnceButNotCancelled(),
        )
    }
}
