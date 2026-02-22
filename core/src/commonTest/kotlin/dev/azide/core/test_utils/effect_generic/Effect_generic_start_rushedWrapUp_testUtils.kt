package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.impl.Transactions.WrapUpContext
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import dev.azide.core.impl.utils.map
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.TestSubjectObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectObserver
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Effect_generic_start_rushedWrapUp_testUtils {
    data class InputStimulationPlan(
        /**
         * Input stimulation before the test subject effect was started.
         */
        val preStartStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect was started.
         */
        val postStartStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect construction was wrapped up.
         */
        val postWrapUpStimulation: TestStimulation,
    )

    fun <SubjectT, NotificationT : Any> testStart(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectEffect: Effect<SubjectT>,
        inputStimulationPlan: InputStimulationPlan? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
        expectedTargetImpact = expectedTargetImpact,
        expectedNewState = expectedSubjectTransition.expectedNewState,
    ) { propagationContext ->
        // 0. Pre-stimulation
        inputStimulationPlan?.preStartStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        val (
            subject: SubjectT,
            subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
        ) = WrapUpContext.wrapUp(
            propagationContext = propagationContext,
        ) { wrapUpContext ->
            LoopUtils.looped { loopedEffectOutcomeLazy: Lazy<Effect.Outcome<SubjectT>> ->
                val loopedSubjectLazy: Lazy<SubjectT> = loopedEffectOutcomeLazy.map { it.result }

                val subjectObserver = TestSubjectObserver.prepare(
                    trait = trait,
                    subjectLazy = loopedSubjectLazy,
                )

                // Observe the subject later in a wrap-up operation, before the subject itself had a chance to
                // wrap up (hence the "rush"). This is the earliest legal point to attempt perceiving the subject.
                subjectObserver.observeLater(
                    wrapUpContext = wrapUpContext,
                )

                // 1. Start the effect
                val effectOutcome: Effect.Outcome<SubjectT> = subjectEffect.start.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                ).result

                inputStimulationPlan?.postStartStimulation?.stimulate(
                    propagationContext = propagationContext,
                )

                LoopClosure(
                    result = Pair(
                        effectOutcome.result,
                        subjectObserver,
                    ),
                    loopedValue = effectOutcome,
                )
            }
        }

        // 2. Post-wrap-up stimulation
        inputStimulationPlan?.postWrapUpStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        expectedSubjectTransition.expectedOldState.verifyStableState(
            propagationContext = propagationContext,
            subject = subject,
        )

        expectedSubjectTransition.expectedReaction.verifyReaction(
            trait = trait,
            subject = subject,
            subjectObserver = subjectObserver,
        )

        subject
    }

    fun <SubjectT, NotificationT : Any> testStart(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = testStart(
        trait = trait,
        subjectEffect = subjectEffect,
        inputStimulationPlan = slottedInputStimulation?.let {
            InputStimulationPlan(
                preStartStimulation = it.slotStimulation0,
                postStartStimulation = it.slotStimulation1,
                postWrapUpStimulation = it.slotStimulation2,
            )
        },
        expectedSubjectTransition = expectedSubjectTransition,
        expectedTargetImpact = expectedTargetImpact,
    )
}
