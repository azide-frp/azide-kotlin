package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.impl.Transactions.WrapUpContext
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import dev.azide.core.impl.utils.map
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulationSlot3
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.installLater

@Suppress("ClassName")
data object Effect_generic_start_rushedWrapUp_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
        expectedTargetImpact = expectedTargetImpact,
        expectedNewState = expectedSubjectTransition.expectedNewState,
    ) { propagationContext ->
        // 0. Pre-stimulation
        slottedInputStimulation?.stimulate(
            propagationContext = propagationContext,
            slot = TestStimulationSlot3.Slot0,
        )

        val (
            subject: SubjectT,
            subjectReactionVerifier: TestSubjectReactionVerifier,
        ) = WrapUpContext.wrapUp(
            propagationContext = propagationContext,
        ) { wrapUpContext ->
            LoopUtils.looped { loopedEffectOutcomeLazy: Lazy<Effect.Outcome<SubjectT>> ->
                val loopedSubjectLazy: Lazy<SubjectT> = loopedEffectOutcomeLazy.map { it.result }

                val subjectReactionVerifier = expectedSubjectTransition.expectedReaction.prepareReactionVerifier(
                    propagationContext = propagationContext,
                    subjectLazy = loopedSubjectLazy,
                )

                // Perceive the subject later in a wrap-up operation, before the subject itself had a chance to
                // wrap up (hence the "rush"). This is the earliest legal point to attempt perceiving the subject.
                subjectReactionVerifier.installLater(
                    wrapUpContext = wrapUpContext,
                )

                // 1. Start the effect
                val effectOutcome: Effect.Outcome<SubjectT> = subjectEffect.start.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                ).result

                slottedInputStimulation?.stimulate(
                    propagationContext = propagationContext,
                    slot = TestStimulationSlot3.Slot1,
                )

                LoopClosure(
                    result = Pair(
                        effectOutcome.result,
                        subjectReactionVerifier,
                    ),
                    loopedValue = effectOutcome,
                )
            }
        }

        // 2. Post-wrap-up stimulation
        slottedInputStimulation?.stimulate(
            propagationContext = propagationContext,
            slot = TestStimulationSlot3.Slot2,
        )

        expectedSubjectTransition.expectedOldState.verifyStableState(
            propagationContext = propagationContext,
            subject = subject,
        )

        subjectReactionVerifier.verifyReaction()

        subject
    }
}
