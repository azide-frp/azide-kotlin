package dev.azide.core.collections.reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.actuate
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.SourceEffectReactiveBagTag
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.TargetEffectTag
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.collections.reactive_bag.correctingChange
import dev.azide.core.test_utils.collections.reactive_bag.revokingChange
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_start_quickCancelled_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveBag_actuate_start_quickCancelled_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceEffectBagChanges =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChanges.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectBagChanges =
        slottedStimulationBank_sourceEffectBagChanges.slottedStimulationScenarios[0]

    private val slottedStimulationBank_sourceEffectBagChangesRevoked =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked =
        slottedStimulationBank_sourceEffectBagChangesRevoked.slottedStimulationScenarios[0]

    private val slottedStimulationBank_sourceEffectBagChangesCorrected =
        ReactiveBag_actuate_testUtils.stimulationBank_sourceEffectBagChangesCorrected.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected =
        slottedStimulationBank_sourceEffectBagChangesCorrected.slottedStimulationScenarios[0]

    @Test
    fun test_start_quickCancelled_observed() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_quickCancelled_nonObserved() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start_quickCancelled(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChanges_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_quickCancelled_sourceEffectBagChanges_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChanges_addedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = SourceEffectReactiveBagTag,
                description = ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect4 to targetEffect4,
                        TargetEffectTag.TargetEffect5 to targetEffect5,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChanges_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_quickCancelled_sourceEffectBagChanges_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChanges_removedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = SourceEffectReactiveBagTag,
                description = ChangeDescription(
                    removedTags = setOf(
                        TargetEffectTag.TargetEffect1,
                        TargetEffectTag.TargetEffect3,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChanges_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_quickCancelled_sourceEffectBagChanges_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChanges_replacedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = SourceEffectReactiveBagTag,
                description = ChangeDescription(
                    replacedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect1 to targetEffect1b,
                        TargetEffectTag.TargetEffect2 to targetEffect2b,
                        TargetEffectTag.TargetEffect3 to targetEffect3b,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChanges_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_start_quickCancelled_sourceEffectBagChanges_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChanges_nonObserved() {
        test_start_quickCancelled_sourceEffectBagChanges_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChanges,
        )
    }

    private fun test_start_quickCancelled_sourceEffectBagChanges_mixed(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceReactiveBag.changing(
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
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
                targetEffect5.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesRevoked_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_quickCancelled_sourceEffectBagChangesRevoked_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesRevoked_addedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = SourceEffectReactiveBagTag,
                intermediateDescription = ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect4 to targetEffect4,
                        TargetEffectTag.TargetEffect5 to targetEffect5,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesRevoked_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_quickCancelled_sourceEffectBagChangesRevoked_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesRevoked_removedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = SourceEffectReactiveBagTag,
                intermediateDescription = ChangeDescription(
                    removedTags = setOf(
                        TargetEffectTag.TargetEffect1,
                        TargetEffectTag.TargetEffect3,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesRevoked_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_quickCancelled_sourceEffectBagChangesRevoked_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesRevoked_replacedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = SourceEffectReactiveBagTag,
                intermediateDescription = ChangeDescription(
                    replacedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect1 to targetEffect1b,
                        TargetEffectTag.TargetEffect2 to targetEffect2b,
                        TargetEffectTag.TargetEffect3 to targetEffect3b,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesRevoked_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_start_quickCancelled_sourceEffectBagChangesRevoked_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesRevoked_nonObserved() {
        test_start_quickCancelled_sourceEffectBagChangesRevoked_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked,
        )
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesRevoked_mixed(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = SourceEffectReactiveBagTag,
                intermediateDescription = ChangeDescription(
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
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
                targetEffect5.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesCorrected_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_quickCancelled_sourceEffectBagChangesCorrected_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesCorrected_addedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
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
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5a.expectIsNotStarted(),
                targetEffect5b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesCorrected_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_quickCancelled_sourceEffectBagChangesCorrected_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesCorrected_removedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
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
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesCorrected_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_quickCancelled_sourceEffectBagChangesCorrected_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesCorrected_replacedOnly(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
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
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect2c.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect4b.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesCorrected_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_start_quickCancelled_sourceEffectBagChangesCorrected_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_start_quickCancelled_sourceEffectBagChangesCorrected_nonObserved() {
        test_start_quickCancelled_sourceEffectBagChangesCorrected_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected,
        )
    }

    private fun test_start_quickCancelled_sourceEffectBagChangesCorrected_mixed(
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

        Effect_ReactiveBag_start_quickCancelled_testUtils.executeStartTransaction(
            subjectReactiveBagEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
                tag = SourceEffectReactiveBagTag,
                intermediateDescription = ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect6 to targetEffect6, // not corrected
                        TargetEffectTag.TargetEffect7 to targetEffect7a, // corrected: added differently
                    ),
                    replacedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect1 to targetEffect1b, // not corrected
                        TargetEffectTag.TargetEffect3 to targetEffect3b, // corrected: not replaced
                    ),
                    removedTags = setOf(
                        TargetEffectTag.TargetEffect2, // not corrected
                        TargetEffectTag.TargetEffect4, // corrected: not removed
                    ),
                ),
                correctedDescription = ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect6 to targetEffect6,
                        TargetEffectTag.TargetEffect7 to targetEffect7b,
                    ),
                    replacedElementByTag = mapOf(
                        TargetEffectTag.TargetEffect1 to targetEffect1b,
                    ),
                    removedTags = setOf(
                        TargetEffectTag.TargetEffect2,
                        TargetEffectTag.TargetEffect5, // (not mentioned before)
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                    TargetEffectTag.TargetEffect4 to 40,
                    TargetEffectTag.TargetEffect5 to 50,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndCancelledOnce(),
                targetEffect3a.expectIsStartedOnceAndCancelledOnce(),
                targetEffect4.expectIsStartedOnceAndCancelledOnce(),
                targetEffect5.expectIsStartedOnceAndCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7a.expectIsNotStarted(),
                targetEffect7b.expectIsNotStarted(),
            ),
        )
    }
}
