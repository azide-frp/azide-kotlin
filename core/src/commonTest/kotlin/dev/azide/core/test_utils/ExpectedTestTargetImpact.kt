package dev.azide.core.test_utils

import dev.azide.core.test_utils.ExpectedTestTargetImpact.TargetImpactVerifier
import kotlin.test.assertEquals

interface ExpectedTestTargetImpact {
    interface TargetImpactVerifier {
        fun verifyPostTransaction()
    }

    companion object {
        fun combine(
            vararg expectedStimulations: ExpectedTestTargetImpact,
        ): ExpectedTestTargetImpact = object : ExpectedTestTargetImpact {
            override fun prepareImpactVerifier(): TargetImpactVerifier {
                val subVerifiers = expectedStimulations.map { it.prepareImpactVerifier() }

                return object : TargetImpactVerifier {
                    override fun verifyPostTransaction() {
                        subVerifiers.forEach { it.verifyPostTransaction() }
                    }
                }
            }
        }
    }

    fun prepareImpactVerifier(): TargetImpactVerifier
}
