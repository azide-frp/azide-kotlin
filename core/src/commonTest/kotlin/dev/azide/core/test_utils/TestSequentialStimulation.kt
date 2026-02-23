package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

/**
 * A [TestStimulation] that represents a sequence of consecutive stimulations.
 */
data class TestSequentialStimulation(
    val consecutiveStimulations: List<TestStimulation>,
) : TestStimulation {
    companion object {
        fun concatAll(
            sequences: Collection<TestSequentialStimulation>,
        ): TestSequentialStimulation? {
            return when {
                sequences.isEmpty() -> null
                else -> TestSequentialStimulation(
                    consecutiveStimulations = sequences.flatMap { it.consecutiveStimulations },
                )
            }
        }
    }

    fun withFinalStimulation(
        finalStimulation: TestStimulation,
    ): TestSequentialStimulation = copy(
        consecutiveStimulations = consecutiveStimulations + finalStimulation,
    )

    fun withAppended(
        other: TestSequentialStimulation,
    ): TestSequentialStimulation = copy(
        consecutiveStimulations = consecutiveStimulations + other.consecutiveStimulations,
    )

    override fun stimulate(
        propagationContext: Transactions.PropagationContext,
    ) {
        for (stimulation in consecutiveStimulations) {
            stimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
