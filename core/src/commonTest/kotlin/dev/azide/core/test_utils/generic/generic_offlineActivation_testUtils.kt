package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Committable
import dev.azide.core.impl.Transactions

@Suppress("ClassName")
data object generic_offlineActivation_testUtils {
    fun <SubjectT, NotificationT : Any> testOfflineActivation(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        subjectHealthChecker: TestSubjectHealthChecker<SubjectT, NotificationT>,
    ) {
        val subjectObserver = TestSubjectObserver.prepare(
            trait = trait,
            subject = subject,
        )

        // Build the health check description _before_ the offline activation transaction. The test subject is not
        // expected to observably react to offline activation.
        val healthCheckDescription = subjectHealthChecker.prepareHealthCheck(
            subject = subject,
        )

        Transactions.execute { propagationContext ->
            propagationContext.enqueueForCommitment(
                committable = object : Committable {
                    override fun commit(
                        commitmentContext: Transactions.CommitmentContext,
                    ) {
                        subjectObserver.observe(
                            processingContext = commitmentContext,
                        )
                    }
                },
            )
        }

        TestSubjectHealthChecker.checkHealthActively(
            trait = trait,
            subject = subject,
            subjectObserver = subjectObserver,
            healthCheckDescription = healthCheckDescription,
        )
    }
}
