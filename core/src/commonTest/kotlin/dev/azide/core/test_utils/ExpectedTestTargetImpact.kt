package dev.azide.core.test_utils

interface ExpectedTestTargetImpact {
    data object None : ExpectedTestTargetImpact {
        override fun prepareImpactVerifier(): TargetImpactVerifier = object : TargetImpactVerifier {
            override fun verifyPostTransaction() {
                // There are no expectations
            }
        }
    }

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
