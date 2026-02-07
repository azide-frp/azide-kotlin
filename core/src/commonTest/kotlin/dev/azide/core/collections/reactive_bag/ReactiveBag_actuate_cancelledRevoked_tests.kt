package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.actuate
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.SourceEffectReactiveBagTag
import dev.azide.core.collections.reactive_bag.ReactiveBag_actuate_testUtils.TargetEffectTag
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.collections.reactive_bag.correctingChange
import dev.azide.core.test_utils.collections.reactive_bag.revokingChange
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_cancelledRevoked_testUtils
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotCancelled
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.getAndResetSingleStartRecord
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.asTestSlottedStimulation3
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveBag_actuate_cancelledRevoked_tests {
    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<TestSlotCount.Count3>

    private val slotCount = TestSlotCount.Count3

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
    fun test_cancelledRevoked_observed() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_cancelledRevoked_nonObserved() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    @Test
    fun test_cancelledRevoked_twice() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    private fun test_cancelledRevoked(
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChanges_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelledRevoked_sourceEffectBagChanges_observed_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChanges_observed_addedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
                targetEffect4.expectIsStartedOnceButNotCancelled(),
                targetEffect5.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChanges_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelledRevoked_sourceEffectBagChanges_observed_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChanges_observed_removedOnly(
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsNotCancelled(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChanges_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelledRevoked_sourceEffectBagChanges_observed_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChanges_observed_replacedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)


        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
            ),
        )

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2a.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsNotCancelled(),
                targetEffect1a.expectIsNotStarted(),
                targetEffect2a.expectIsNotStarted(),
                targetEffect3a.expectIsNotStarted(),
                targetEffect1b.expectIsStartedOnceButNotCancelled(),
                targetEffect2b.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChanges_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChanges.forEach {
            test_cancelledRevoked_sourceEffectBagChanges_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChanges_nonObserved() {
        test_cancelledRevoked_sourceEffectBagChanges_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChanges,
        )
    }

    private fun test_cancelledRevoked_sourceEffectBagChanges_mixed(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()
        val targetEffect5StartRecord = targetEffect5.getAndResetSingleStartRecord()

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect3b = TestTargetEffect.pure(result = 31)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7 = TestTargetEffect.pure(result = 70)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect5StartRecord.expectIsNotCancelled(),
                targetEffect1a.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3a.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
                targetEffect1b.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsStartedOnceButNotCancelled(),
                targetEffect6.expectIsStartedOnceButNotCancelled(),
                targetEffect7.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_addedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_removedOnly(
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect4StartRecord.expectIsNotCancelled(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_replacedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
            ),
        )

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2a.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect3b = TestTargetEffect.pure(result = 31)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedContent = mapOf(
                    TargetEffectTag.TargetEffect1 to 10,
                    TargetEffectTag.TargetEffect2 to 20,
                    TargetEffectTag.TargetEffect3 to 30,
                ),
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect1a.expectIsNotStarted(),
                targetEffect2a.expectIsNotStarted(),
                targetEffect3a.expectIsNotStarted(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesRevoked_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesRevoked.forEach {
            test_cancelledRevoked_sourceEffectBagChangesRevoked_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesRevoked_nonObserved() {
        test_cancelledRevoked_sourceEffectBagChangesRevoked_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesRevoked,
        )
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesRevoked_mixed(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()
        val targetEffect5StartRecord = targetEffect5.getAndResetSingleStartRecord()

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect3b = TestTargetEffect.pure(result = 31)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7 = TestTargetEffect.pure(result = 70)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect4StartRecord.expectIsNotCancelled(),
                targetEffect5StartRecord.expectIsNotCancelled(),
                targetEffect1a.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3a.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsNotStarted(),
                targetEffect7.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_addedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_addedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_addedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()

        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5a = TestTargetEffect.pure(result = 50)
        val targetEffect5b = TestTargetEffect.pure(result = 51)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7 = TestTargetEffect.pure(result = 70)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5a.expectIsNotStarted(),
                targetEffect5b.expectIsStartedOnceButNotCancelled(),
                targetEffect6.expectIsStartedOnceButNotCancelled(),
                targetEffect7.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_removedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_removedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_removedOnly(
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

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsNotCancelled(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_replacedOnly() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_replacedOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_replacedOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2a = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4a = TestTargetEffect.pure(result = 40)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2a,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4a,
            ),
        )

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2a.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4a.getAndResetSingleStartRecord()

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect2b = TestTargetEffect.pure(result = 21)
        val targetEffect2c = TestTargetEffect.pure(result = 22)
        val targetEffect3b = TestTargetEffect.pure(result = 31)
        val targetEffect4b = TestTargetEffect.pure(result = 41)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsCancelledOnce(),
                targetEffect4StartRecord.expectIsCancelledOnce(),
                targetEffect1a.expectIsNotStarted(),
                targetEffect2a.expectIsNotStarted(),
                targetEffect3a.expectIsNotStarted(),
                targetEffect4a.expectIsNotStarted(),
                targetEffect1b.expectIsNotStarted(),
                targetEffect2b.expectIsNotStarted(),
                targetEffect2c.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsStartedOnceButNotCancelled(),
                targetEffect4b.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesCorrected_observed_mixed() {
        slottedStimulationBank_sourceEffectBagChangesCorrected.forEach {
            test_cancelledRevoked_sourceEffectBagChangesCorrected_mixed(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = it,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEffectBagChangesCorrected_nonObserved() {
        test_cancelledRevoked_sourceEffectBagChangesCorrected_mixed(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectBagChangesCorrected,
        )
    }

    private fun test_cancelledRevoked_sourceEffectBagChangesCorrected_mixed(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1a = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3a = TestTargetEffect.pure(result = 30)
        val targetEffect4 = TestTargetEffect.pure(result = 40)
        val targetEffect5 = TestTargetEffect.pure(result = 50)

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedContent = mapOf(
                TargetEffectTag.TargetEffect1 to targetEffect1a,
                TargetEffectTag.TargetEffect2 to targetEffect2,
                TargetEffectTag.TargetEffect3 to targetEffect3a,
                TargetEffectTag.TargetEffect4 to targetEffect4,
                TargetEffectTag.TargetEffect5 to targetEffect5,
            ),
        )

        val subjectOutcome = sourceReactiveBag.actuate().startExternally()

        val targetEffect1StartRecord = targetEffect1a.getAndResetSingleStartRecord()
        val targetEffect2StartRecord = targetEffect2.getAndResetSingleStartRecord()
        val targetEffect3StartRecord = targetEffect3a.getAndResetSingleStartRecord()
        val targetEffect4StartRecord = targetEffect4.getAndResetSingleStartRecord()
        val targetEffect5StartRecord = targetEffect5.getAndResetSingleStartRecord()

        val targetEffect1b = TestTargetEffect.pure(result = 11)
        val targetEffect3b = TestTargetEffect.pure(result = 31)
        val targetEffect6 = TestTargetEffect.pure(result = 60)
        val targetEffect7a = TestTargetEffect.pure(result = 70)
        val targetEffect7b = TestTargetEffect.pure(result = 71)

        Effect_ReactiveBag_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
            ).asTestSlottedStimulation3,
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2StartRecord.expectIsCancelledOnce(),
                targetEffect3StartRecord.expectIsNotCancelled(),
                targetEffect4StartRecord.expectIsNotCancelled(),
                targetEffect5StartRecord.expectIsCancelledOnce(),
                targetEffect1a.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3a.expectIsNotStarted(),
                targetEffect4.expectIsNotStarted(),
                targetEffect5.expectIsNotStarted(),
                targetEffect1b.expectIsStartedOnceButNotCancelled(),
                targetEffect3b.expectIsNotStarted(),
                targetEffect6.expectIsStartedOnceButNotCancelled(),
                targetEffect7a.expectIsNotStarted(),
                targetEffect7b.expectIsStartedOnceButNotCancelled(),
            ),
        )

        ReactiveBag_actuate_testUtils.verifyEffectOngoing(
            sourceReactiveBag = sourceReactiveBag,
            subjectReactiveBag = subjectOutcome.result,
        )
    }
}
