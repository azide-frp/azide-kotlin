package dev.azide.core.test_utils.generic

import dev.azide.core.Moment
import dev.azide.core.impl.Transactions.WrapUpContext
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulationSlot3
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.TestSubjectReactionVerifier

@Suppress("ClassName")
data object generic_spawn_rushedWrapUp_testUtils {
    fun <SubjectT : Any> executeSpawnTransaction(
        subjectSpawnMoment: Moment<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
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
                LoopUtils.looped { loopedSubjectLazy: Lazy<SubjectT> ->
                    val subjectReactionVerifier = expectedSubjectTransition.expectedReaction.prepareReactionVerifier(
                        propagationContext = propagationContext,
                        subjectLazy = loopedSubjectLazy,
                    )

                    // Perceive the subject later in a wrap-up operation, before the subject itself had a chance to
                    // wrap up (hence the "rush"). This is the earliest legal point to attempt perceiving the subject.
                    subjectReactionVerifier.installLater(
                        wrapUpContext = wrapUpContext,
                    )

                    // 1. Spawn the subject
                    val subject = subjectSpawnMoment.pullInternally(
                        propagationContext = propagationContext,
                        wrapUpContext = wrapUpContext,
                    )

                    slottedInputStimulation?.stimulate(
                        propagationContext = propagationContext,
                        slot = TestStimulationSlot3.Slot1,
                    )

                    LoopClosure(
                        result = Pair(
                            subject,
                            subjectReactionVerifier,
                        ),
                        loopedValue = subject,
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
}
