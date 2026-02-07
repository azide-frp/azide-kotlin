package dev.azide.core.collections.reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.actuate
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.SourceEffectReactiveBagTag
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.TargetEffectTag
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.collections.reactive_bag.correctingChange
import dev.azide.core.test_utils.collections.reactive_bag.revokingChange
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.asTestSlottedStimulation4
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveBag_actuate_start_quickCancelledRevoked_tests {
    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<TestSlotCount.Count4>

    private val slotCount = TestSlotCount.Count4

    private val slottedStimulationBank_sourceEffectBagChanges =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChanges.distribute(slotCount = slotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectBagChanges =
        slottedStimulationBank_sourceEffectBagChanges.slottedStimulationScenarios[0]

    private val slottedStimulationBank_sourceEffectBagChangesRevoked =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChangesRevoked.distribute(slotCount = slotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked =
        slottedStimulationBank_sourceEffectBagChangesRevoked.slottedStimulationScenarios[0]

    private val slottedStimulationBank_sourceEffectBagChangesCorrected =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChangesCorrected.distribute(slotCount = slotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected =
        slottedStimulationBank_sourceEffectBagChangesCorrected.slottedStimulationScenarios[0]

    @Test
    fun test_start_observed() {
        test_start(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_nonObserved() {
        test_start(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
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

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
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

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChanges_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_sourceEffectBagChanges_observed_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChanges_observed_addedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.changing(
                    tag = SourceEffectReactiveBagTag,
                    description = ChangeDescription(
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect4 to targetEffect4,
                            TargetEffectTag.TargetEffect5 to targetEffect5,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
                targetEffect5.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChanges_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_sourceEffectBagChanges_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChanges_removedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.changing(
                    tag = SourceEffectReactiveBagTag,
                    description = ChangeDescription(
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect1,
                            TargetEffectTag.TargetEffect3,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChanges_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_sourceEffectBagChanges_observed_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChanges_observed_replacedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.changing(
                    tag = SourceEffectReactiveBagTag,
                    description = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b,
                            TargetEffectTag.TargetEffect2 to targetEffect2b,
                            TargetEffectTag.TargetEffect3 to targetEffect3b,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 11,
                    TargetEffectTag.TargetEffect2 to 21,
                    TargetEffectTag.TargetEffect3 to 31,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
                targetEffect1b.expectIsStartedOnceButNotCancelled(),
                targetEffect2b.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChanges_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_sourceEffectBagChanges_observed_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_start_sourceEffectBagChanges_nonObserved() {
        test_start_sourceEffectBagChanges_observed_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChanges,
        )
    }

    private fun test_start_sourceEffectBagChanges_observed_mixed(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
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

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.changing(
                    tag = SourceEffectReactiveBagTag,
                    description = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b, // updated
                            TargetEffectTag.TargetEffect3 to targetEffect3b, // updated
                        ),
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect6 to targetEffect6, // added
                            TargetEffectTag.TargetEffect7 to targetEffect7, // added
                        ),
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect2,
                            TargetEffectTag.TargetEffect4,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 11,
                    TargetEffectTag.TargetEffect3 to 31,
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

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesRevoked_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_sourceEffectBagChangesRevoked_observed_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChangesRevoked_observed_addedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.revokingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect4 to targetEffect4,
                            TargetEffectTag.TargetEffect5 to targetEffect5,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
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
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesRevoked_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_sourceEffectBagChangesRevoked_observed_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChangesRevoked_observed_removedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.revokingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect1,
                            TargetEffectTag.TargetEffect3,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesRevoked_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_sourceEffectBagChangesRevoked_observed_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChangesRevoked_observed_replacedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.revokingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b,
                            TargetEffectTag.TargetEffect2 to targetEffect2b,
                            TargetEffectTag.TargetEffect3 to targetEffect3b,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceButNotCancelled(),
                targetEffect2a.expectIsStartedOnceButNotCancelled(),
                targetEffect3a.expectIsStartedOnceButNotCancelled(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesRevoked_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_sourceEffectBagChangesRevoked_observed_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_start_sourceEffectBagChangesRevoked_nonObserved() {
        test_start_sourceEffectBagChangesRevoked_observed_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked,
        )
    }

    private fun test_start_sourceEffectBagChangesRevoked_observed_mixed(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
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

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.revokingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b, // updated
                            TargetEffectTag.TargetEffect3 to targetEffect3b, // updated
                        ),
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect6 to targetEffect6, // added
                            TargetEffectTag.TargetEffect7 to targetEffect7, // added
                        ),
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect2,
                            TargetEffectTag.TargetEffect4,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3a.expectIsStartedOnceButNotCancelled(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
                targetEffect5.expectIsStartedOnceButNotCancelled(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesCorrected_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_sourceEffectBagChangesCorrected_observed_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChangesCorrected_observed_addedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5a = TestTargetEffect.pure(result = 50)
        val targetEffect5b = TestTargetEffect.pure(result = 51)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7 = TestTargetEffect.pure(result = 70)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.correctingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect4 to targetEffect4, // corrected: not added
                            TargetEffectTag.TargetEffect5 to targetEffect5a, // corrected: added differently
                            TargetEffectTag.TargetEffect7 to targetEffect7, // not corrected
                        ),
                    ),
                    correctedDescription = ChangeDescription(
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect5 to targetEffect5b,
                            TargetEffectTag.TargetEffect6 to targetEffect6, // (not mentioned before)
                            TargetEffectTag.TargetEffect7 to targetEffect7,
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect5 to 51,
                    TargetEffectTag.TargetEffect6 to 60,
                    TargetEffectTag.TargetEffect7 to 70,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5a.expectIsNotStarted(),
                targetEffect5b.expectIsStartedOnceButNotCancelled(),
                targetEffect6.expectIsStartedOnceButNotCancelled(),
                targetEffect7.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesCorrected_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_sourceEffectBagChangesCorrected_observed_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChangesCorrected_observed_removedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.correctingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect1, // corrected: not removed
                            TargetEffectTag.TargetEffect3, // not corrected
                        ),
                    ),
                    correctedDescription = ChangeDescription(
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect3,
                            TargetEffectTag.TargetEffect4, // (not mentioned before)
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesCorrected_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_sourceEffectBagChangesCorrected_observed_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_sourceEffectBagChangesCorrected_observed_replacedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4a = TestTargetEffect.pure(result = 40)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect2c = TestTargetEffect.pure(result = 22)
        val targetEffect3b = TestTargetEffect.pure(result = 31)
        val targetEffect4b = TestTargetEffect.pure(result = 41)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4a,
            ),
        )

        val subjectEffect: Effect<ReactiveBag<Int>> = sourceReactiveBag.actuate()

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.correctingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b, // corrected: not replaced
                            TargetEffectTag.TargetEffect2 to targetEffect2b, // corrected: replaced differently
                            TargetEffectTag.TargetEffect3 to targetEffect3b, // not corrected
                        ),
                    ),
                    correctedDescription = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect2 to targetEffect2c,
                            TargetEffectTag.TargetEffect3 to targetEffect3b,
                            TargetEffectTag.TargetEffect4 to targetEffect4b, // (not mentioned before)
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 22,
                    TargetEffectTag.TargetEffect3 to 31,
                    TargetEffectTag.TargetEffect4 to 41,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceButNotCancelled(),
                targetEffect2a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect2c.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsStartedOnceButNotCancelled(),
                targetEffect4b.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }

    @Test
    fun test_start_sourceEffectBagChangesCorrected_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_sourceEffectBagChangesCorrected_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_start_sourceEffectBagChangesCorrected_nonObserved() {
        test_start_sourceEffectBagChangesCorrected_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected,
        )
    }

    private fun test_start_sourceEffectBagChangesCorrected_mixed(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
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
        val targetEffect7a = TestTargetEffect.pure(result = 70)
        val targetEffect7b = TestTargetEffect.pure(result = 71)

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

        val subjectReactiveBag = Effect_ReactiveBag_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.correctingChange(
                    tag = SourceEffectReactiveBagTag,
                    intermediateDescription = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b, // not corrected
                            TargetEffectTag.TargetEffect3 to targetEffect3b, // corrected: not replaced
                        ),
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect6 to targetEffect6, // not corrected
                            TargetEffectTag.TargetEffect7 to targetEffect7a, // corrected: added differently
                        ),
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect2, // not corrected
                            TargetEffectTag.TargetEffect4, // corrected: not removed
                        ),
                    ),
                    correctedDescription = ChangeDescription(
                        replacedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect1 to targetEffect1b,
                        ),
                        addedElementByTag = mapOf(
                            TargetEffectTag.TargetEffect6 to targetEffect6,
                            TargetEffectTag.TargetEffect7 to targetEffect7b,
                        ),
                        removedTags = setOf(
                            TargetEffectTag.TargetEffect2,
                            TargetEffectTag.TargetEffect5, // (not mentioned before)
                        ),
                    ),
                ),
            ).asTestSlottedStimulation4,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
                expectedNewTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 11,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect6 to 60,
                    TargetEffectTag.TargetEffect7 to 71,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceButNotCancelled(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
                targetEffect5.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsStartedOnceButNotCancelled(),
                targetEffect7a.expectIsNotStarted(),
                targetEffect7b.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectReactiveBag,
        )
    }
}
