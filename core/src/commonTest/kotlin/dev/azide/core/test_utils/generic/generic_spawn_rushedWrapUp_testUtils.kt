package dev.azide.core.test_utils.generic

import dev.azide.core.Moment
import dev.azide.core.impl.Transactions.WrapUpContext
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object generic_spawn_rushedWrapUp_testUtils {
    fun <SubjectT : Any, NotificationT : Any> executeSpawnTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectSpawnMoment: Moment<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            val (
                subject: SubjectT,
                subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
            ) = WrapUpContext.wrapUp(
                propagationContext = propagationContext,
            ) { wrapUpContext ->
                LoopUtils.looped { loopedSubjectLazy: Lazy<SubjectT> ->
                    // Observe the subject later in a wrap-up operation, before the subject itself had a chance to
                    // wrap up (hence the "rush"). This is the earliest legal point to attempt perceiving the subject.
                    val subjectObserver = TestSubjectObserver(
                        trait = trait,
                        subjectLazy = loopedSubjectLazy,
                    )

                    // 1. Spawn the subject
                    val subject = subjectSpawnMoment.pullInternally(
                        propagationContext = propagationContext,
                        wrapUpContext = wrapUpContext,
                    )

                    subjectObserver.observeLater(
                        wrapUpContext = wrapUpContext,
                    )

                    slottedInputStimulation?.slotStimulation1?.stimulate(
                        propagationContext = propagationContext,
                    )

                    LoopClosure(
                        result = Pair(
                            subject,
                            subjectObserver,
                        ),
                        loopedValue = subject,
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

            expectedSubjectTransition.expectedReaction.verifyReaction(
                trait = trait,
                subject = subject,
                subjectObserver = subjectObserver,
            )

            subject
        }
    }
}
