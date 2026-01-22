package dev.azide.core.test_utils

interface ExpectedTestTargetImpact {
    data object None : ExpectedTestTargetImpact {
        override fun prepareImpactVerifier(): TargetImpactVerifier = object : TargetImpactVerifier {
            override fun verifyPostPropagation() {
                // There are no expectations
            }
        }
    }

    interface TargetImpactVerifier {
        fun verifyPostPropagation()
    }

    companion object {
        fun combine(
            vararg expectedStimulations: ExpectedTestTargetImpact,
        ): ExpectedTestTargetImpact = object : ExpectedTestTargetImpact {
            override fun prepareImpactVerifier(): TargetImpactVerifier {
                val subVerifiers = expectedStimulations.map { it.prepareImpactVerifier() }

                return object : TargetImpactVerifier {
                    override fun verifyPostPropagation() {
                        subVerifiers.forEach { it.verifyPostPropagation() }
                    }
                }
            }
        }
    }

    fun prepareImpactVerifier(): TargetImpactVerifier
}
