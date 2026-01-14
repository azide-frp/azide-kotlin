package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

interface ExpectedTestSubjectReaction<SubjectT, SubjectProxyT> {
    interface DeltaVerifier {
        fun verifyExposedCorrectly()

        fun verifyPropagatedCorrectly()

    }

    interface NewStateVerifier {
        fun verifyNewState(
            propagationContext: Transactions.PropagationContext,
        )
    }

    enum class IntermediatePropagationTolerance {
        DoNotTolerate, Tolerate,
    }

    fun prepareDeltaVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectProxy: SubjectProxyT,
    ): DeltaVerifier

    fun prepareNewStateVerifier(
        propagationContext: Transactions.PropagationContext,
        subject: SubjectT,
    ): NewStateVerifier
}

fun ExpectedTestSubjectReaction.DeltaVerifier.verifyExposedAndPropagatedCorrectly() {
    verifyExposedCorrectly()
    verifyPropagatedCorrectly()
}

fun <SubjectT, SubjectProxyT> ExpectedTestSubjectReaction<SubjectT, SubjectProxyT>.verifyDeltaExposedCorrectly(
    propagationContext: Transactions.PropagationContext,
    subjectProxy: SubjectProxyT,
) {
    prepareDeltaVerifier(
        propagationContext = propagationContext,
        subjectProxy = subjectProxy,
    ).verifyExposedCorrectly()
}
