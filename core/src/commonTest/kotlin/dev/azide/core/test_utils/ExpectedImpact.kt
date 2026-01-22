package dev.azide.core.test_utils

import kotlin.test.assertEquals

interface ExpectedImpact {
    data object None : ExpectedImpact {
        override fun prepareImpactVerifier(): ImpactVerifier = object : ImpactVerifier {
            override fun verifyPostPropagation() {
                // There are no expectations
            }
        }
    }

    interface ImpactVerifier {
        fun verifyPostPropagation()
    }

    companion object {
        fun combine(
            vararg expectedStimulations: ExpectedImpact,
        ): ExpectedImpact = object : ExpectedImpact {
            override fun prepareImpactVerifier(): ImpactVerifier {
                val subVerifiers = expectedStimulations.map { it.prepareImpactVerifier() }

                return object : ImpactVerifier {
                    override fun verifyPostPropagation() {
                        subVerifiers.forEach { it.verifyPostPropagation() }
                    }
                }
            }
        }

        fun expectUnmodified(
            externalList: List<*>,
        ): ExpectedImpact = object : ExpectedImpact {
            override fun prepareImpactVerifier(): ImpactVerifier {
                val originalSnapshot = externalList.toList()

                return object : ImpactVerifier {
                    override fun verifyPostPropagation() {
                        assertEquals(
                            expected = originalSnapshot,
                            actual = externalList,
                            message = "Expected the external list to remain unmodified during the propagation.",
                        )
                    }
                }
            }
        }
    }

    fun prepareImpactVerifier(): ImpactVerifier
}
