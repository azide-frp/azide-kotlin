package dev.azide.core.collections.reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.actuate
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.SourceEffectReactiveBagTag
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.TargetEffectTag
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.asTestSlottedStimulation3
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveBag_actuate_start_rushedWrapUp_tests {
    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<TestSlotCount.Count3>

    private val slotCount = TestSlotCount.Count3

    private val slottedStimulationBank_sourceEffectBagChanges =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChanges.distribute(slotCount = slotCount)

    @Test
    fun test_start_rushedWrapUp() {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_rushedWrapUp_sourceEffectBagChanges() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_rushedWrapUp_sourceEffectBagChanges(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_rushedWrapUp_sourceEffectBagChanges(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect3b = TestTargetEffect.pure(result = 31)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7 = TestTargetEffect.pure(result = 70)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.changing(
                    tag = SourceEffectReactiveBagTag,
                    description = ChangeDescription(
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect6 to targetEffect6,
                            TargetEffectTag.TargetEffect7 to targetEffect7,
                        ),
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b,
                            TargetEffectTag.TargetEffect3 to targetEffect3b,
                        ),
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect2,
                            TargetEffectTag.TargetEffect4,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation3,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 11,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 31,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                    TargetEffectTag.TargetEffect6 to 60,
                    TargetEffectTag.TargetEffect7 to 70,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
                targetEffect5.expectIsStartedOnceButNotCancelled(),
                targetEffect1b.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsStartedOnceButNotCancelled(),
                targetEffect6.expectIsStartedOnceButNotCancelled(),
                targetEffect7.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }
}
