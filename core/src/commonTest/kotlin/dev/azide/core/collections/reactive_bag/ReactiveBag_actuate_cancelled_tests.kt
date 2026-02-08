package dev.azide.core.collections.reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.actuate
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.SourceEffectReactiveBagConstructionStrategy
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.SourceEffectReactiveBagTag
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.TargetEffectTag
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.startExternallyPreStimulated
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.collections.reactive_bag.correctingChange
import dev.azide.core.test_utils.collections.reactive_bag.revokingChange
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_cancelled_testUtils
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.getAndResetSingleStartRecord
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveBag_actuate_cancelled_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

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
    fun test_initial_cancelled_observed() {
        test_cancelled(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_initial_cancelled_nonObserved() {
        test_cancelled(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    @Test
    fun test_subsequent_cancelled_observed() {
        test_cancelled(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_subsequent_cancelled_nonObserved() {
        test_cancelled(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    @Test
    fun test_cancelled_twice() {
        test_cancelled(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            cancelCount = 2,
        )
    }

    private fun test_cancelled(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        cancelCount: Int = 1,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
            ),
            cancelCount = cancelCount,
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChanges_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_addedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChanges_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_addedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChanges_addedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChanges_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_removedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChanges_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_removedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChanges_removedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChanges_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_replacedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChanges_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_replacedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChanges_replacedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2a.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChanges_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_mixed(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChanges_nonObserved() {
        test_cancelled_sourceEffectBagChanges_mixed(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChanges,
        )
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChanges_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelled_sourceEffectBagChanges_mixed(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChanges_nonObserved() {
        test_cancelled_sourceEffectBagChanges_mixed(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChanges,
        )
    }

    private fun test_cancelled_sourceEffectBagChanges_mixed(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7 = TestTargetEffect.pure(result = 70)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()
        val targetEffect5StartRecord = targetEffect5.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
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
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect5StartRecord.expectIsCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesRevoked_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_addedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesRevoked_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_addedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChangesRevoked_addedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesRevoked_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_removedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesRevoked_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_removedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChangesRevoked_removedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesRevoked_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_replacedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesRevoked_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_replacedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChangesRevoked_replacedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2a.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesRevoked_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_mixed(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesRevoked_nonObserved() {
        test_cancelled_sourceEffectBagChangesRevoked_mixed(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked,
        )
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesRevoked_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelled_sourceEffectBagChangesRevoked_mixed(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesRevoked_nonObserved() {
        test_cancelled_sourceEffectBagChangesRevoked_mixed(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked,
        )
    }

    private fun test_cancelled_sourceEffectBagChangesRevoked_mixed(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
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

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()
        val targetEffect5StartRecord = targetEffect5.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.revokingChange(
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
                ),
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect5StartRecord.expectIsCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesCorrected_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_addedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesCorrected_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_addedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChangesCorrected_addedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
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

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5a.expectIsNotStarted(),
                targetEffect5b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesCorrected_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_removedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesCorrected_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_removedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChangesCorrected_removedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesCorrected_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_replacedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesCorrected_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_replacedOnly(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelled_sourceEffectBagChangesCorrected_replacedOnly(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
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

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4a,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2a.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4a.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect2c.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect4b.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesCorrected_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_mixed(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_initial_cancelled_sourceEffectBagChangesCorrected_nonObserved() {
        test_cancelled_sourceEffectBagChangesCorrected_mixed(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.InitialContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected,
        )
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesCorrected_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelled_sourceEffectBagChangesCorrected_mixed(
                sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_subsequent_cancelled_sourceEffectBagChangesCorrected_nonObserved() {
        test_cancelled_sourceEffectBagChangesCorrected_mixed(
            sourceEffectReactiveBagConstructionStrategy = SourceEffectReactiveBagConstructionStrategy.SubsequentContentStrategy,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected,
        )
    }

    private fun test_cancelled_sourceEffectBagChangesCorrected_mixed(
        sourceEffectReactiveBagConstructionStrategy: SourceEffectReactiveBagConstructionStrategy,
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

        val (sourceReactiveBag, preStimulation) = sourceEffectReactiveBagConstructionStrategy.construct(
            taggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = sourceReactiveBag.actuate().startExternallyPreStimulated(
            preStimulation = preStimulation,
        )

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()
        val targetEffect5StartRecord = targetEffect5.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = sourceReactiveBag.correctingChange(
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
                ),
            ),
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect5StartRecord.expectIsCancelledOnce(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7a.expectIsNotStarted(),
                targetEffect7b.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectNotOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }
}
