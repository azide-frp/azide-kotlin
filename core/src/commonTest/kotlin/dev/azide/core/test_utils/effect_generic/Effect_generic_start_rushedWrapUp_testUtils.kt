package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.impl.Transactions.WrapUpContext
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import dev.azide.core.impl.utils.map
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.installLater
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Effect_generic_start_rushedWrapUp_testUtils {
    fun <SubjectT, NotificationT : Any> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
        expectedTargetImpact = expectedTargetImpact,
        expectedNewState = expectedSubjectTransition.expectedNewState,
    ) { propagationContext ->
        // 0. Pre-stimulation
        slottedInputStimulation?.slotStimulation0?.stimulate(
            propagationContext = propagationContext,
        )

        val (
            subject: SubjectT,
            subjectReactionVerifier: TestSubjectReactionVerifier<SubjectT, NotificationT>,
        ) = WrapUpContext.wrapUp(
            propagationContext = propagationContext,
        ) { wrapUpContext ->
            LoopUtils.looped { loopedEffectOutcomeLazy: Lazy<Effect.Outcome<SubjectT>> ->
                val loopedSubjectLazy: Lazy<SubjectT> = loopedEffectOutcomeLazy.map { it.result }

                val subjectReactionVerifier = expectedSubjectTransition.expectedReaction.prepareReactionVerifier(
                    propagationContext = propagationContext,
                    subjectLazy = loopedSubjectLazy,
                )

                // Observe the subject later in a wrap-up operation, before the subject itself had a chance to
                // wrap up (hence the "rush"). This is the earliest legal point to attempt perceiving the subject.
                subjectReactionVerifier.installLater(
                    wrapUpContext = wrapUpContext,
                )

                // 1. Start the effect
                val effectOutcome: Effect.Outcome<SubjectT> = subjectEffect.start.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                ).result

                slottedInputStimulation?.slotStimulation1?.stimulate(
                    propagationContext = propagationContext,
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
        slottedInputStimulation?.slotStimulation2?.stimulate(
            propagationContext = propagationContext,
        )

        expectedSubjectTransition.expectedOldState.verifyStableState(
            propagationContext = propagationContext,
            subject = subject,
        )

        subjectReactionVerifier.verifyReaction()

        subject
    }
}
