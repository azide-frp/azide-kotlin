package dev.azide.core.collections.reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.sampleTaggedContentExternally
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.utils.list.uncons
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.effect_reactive_bag.Effect_ReactiveBag_step_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object ReactiveBag_actuate_testUtils {
    data object SourceEffectReactiveBagTag : TestInputReactiveCollectionTag

    enum class TargetEffectTag {
        EarlierTargetEffect1, EarlierTargetEffect2, TargetEffect1, TargetEffect2, TargetEffect3, TargetEffect4, TargetEffect5, TargetEffect6, TargetEffect7, ExtraTargetEffect,
    }

    interface SourceEffectReactiveBagConstructionStrategy {
        interface PreStimulation {
            data object None : PreStimulation {
                override fun preStimulateExternally() {
                }
            }

            fun preStimulateExternally()
        }

        data object InitialContentStrategy : SourceEffectReactiveBagConstructionStrategy {
            override fun construct(
                taggedContent: Map<ReactiveBag.Tag, Effect<Int>>,
            ): Pair<TestInputReactiveBag<Effect<Int>>, PreStimulation> = Pair(
                TestInputReactiveBag(
                    initialTaggedContent = taggedContent,
                ),
                PreStimulation.None,
            )
        }

        data object SubsequentContentStrategy : SourceEffectReactiveBagConstructionStrategy {
            override fun construct(
                taggedContent: Map<ReactiveBag.Tag, Effect<Int>>,
            ): Pair<TestInputReactiveBag<Effect<Int>>, PreStimulation> {
                val taggedContentEntries = taggedContent.entries.toList()
                val (firstTaggedContentEntry, remainingTaggedContentEntries) = taggedContentEntries.uncons()
                    ?: throw IllegalArgumentException("taggedContent must have at least two entries")
                val (firstTag, firstEffect) = firstTaggedContentEntry

                val earlierTargetEffect1 = TestTargetEffect.pure(result = -1)
                val earlierTargetEffect2 = TestTargetEffect.pure(result = -2)
                val earlierTargetEffect3 = TestTargetEffect.pure(result = -3)

                val sourceReactiveBag = TestInputReactiveBag<Effect<Int>>(
                    initialTaggedContent = mapOf(
                        TargetEffectTag.EarlierTargetEffect1 to earlierTargetEffect1,
                        TargetEffectTag.EarlierTargetEffect2 to earlierTargetEffect2,
                        firstTag to earlierTargetEffect3,
                    ),
                )

                return Pair(
                    sourceReactiveBag,
                    object : PreStimulation {
                        override fun preStimulateExternally() {
                            Transactions.execute { propagationContext ->
                                sourceReactiveBag.change(
                                    changeDescription = TestInputReactiveBag.ChangeDescription(
                                        addedElementByTag = remainingTaggedContentEntries.associate {
                                            it.key to it.value
                                        },
                                        replacedElementByTag = mapOf(
                                            firstTag to firstEffect,
                                        ),
                                        removedTags = setOf(
                                            TargetEffectTag.EarlierTargetEffect1,
                                            TargetEffectTag.EarlierTargetEffect2,
                                        ),
                                    ),
                                ).stimulate(
                                    propagationContext = propagationContext,
                                )
                            }
                        }
                    },
                )
            }
        }

        fun construct(
            taggedContent: Map<ReactiveBag.Tag, Effect<Int>>,
        ): Pair<TestInputReactiveBag<Effect<Int>>, PreStimulation>
    }

    fun Effect<ReactiveBag<Int>>.startExternallyPreStimulated(
        preStimulation: SourceEffectReactiveBagConstructionStrategy.PreStimulation,
    ): Effect.Outcome<ReactiveBag<Int>> {
        val subjectOutcome: Effect.Outcome<ReactiveBag<Int>> = startExternally()

        preStimulation.preStimulateExternally()

        return subjectOutcome
    }

    val stimulationScenarioBank_sourceEffectBagChanges = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveBagTag,
        ),
    )

    val stimulationScenarioBank_sourceEffectBagChangesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveBagTag,
        ),
    )

    val stimulationScenarioBank_sourceEffectBagChangesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveBagTag,
        ),
    )

    fun verifyEffectNotOngoing(
        sourceReactiveBag: TestInputReactiveBag<Effect<Int>>,
        subjectReactiveBag: ReactiveBag<Int>,
    ) {
        val preStimulationTaggedContent = subjectReactiveBag.sampleTaggedContentExternally()

        val extraTargetEffect = TestTargetEffect.pure(result = -1)

        Effect_ReactiveBag_step_testUtils.testStep(
            subjectReactiveBag = subjectReactiveBag,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.change(
                changeDescription = TestInputReactiveBag.ChangeDescription(
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

        generic_testUtils.executeTransactionWithImpactVerification(
            inputStimulation = sourceReactiveBag.change(
                changeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        TargetEffectTag.ExtraTargetEffect to extraTargetEffect,
                    ),
                ),
            ),
            expectedTargetImpact = extraTargetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectOngoing(
        sourceReactiveBag: TestInputReactiveBag<TestTargetEffect<Int>>,
        subjectReactiveBag: ReactiveBag<Int>,
    ) {
        val preStimulationContent = subjectReactiveBag.sampleTaggedContentExternally()

        val extraTargetEffect = TestTargetEffect.pure(result = 0)

        Effect_ReactiveBag_step_testUtils.testStep(
            subjectReactiveBag = subjectReactiveBag,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            slottedInputStimulation = sourceReactiveBag.change(
                changeDescription = TestInputReactiveBag.ChangeDescription(
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
