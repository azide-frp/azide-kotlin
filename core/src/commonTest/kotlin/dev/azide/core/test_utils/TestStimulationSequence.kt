package dev.azide.core.test_utils

data class TestStimulationSequence(
    val consecutiveStimulations: List<TestStimulation>,
) {
    companion object {
        fun concatAll(
            sequences: Collection<TestStimulationSequence>,
        ): TestStimulationSequence? {
            return when {
                sequences.isEmpty() -> null
                else -> TestStimulationSequence(
                    consecutiveStimulations = sequences.flatMap { it.consecutiveStimulations },
                )
            }
        }
    }

    init {
        require(consecutiveStimulations.isNotEmpty()) {
            "A TestStimulationSequence cannot be empty."
        }
    }

    fun withFinalStimulation(
        finalStimulation: TestStimulation,
    ): TestStimulationSequence = copy(
        consecutiveStimulations = consecutiveStimulations + finalStimulation,
    )

    fun withAppended(
        other: TestStimulationSequence,
    ): TestStimulationSequence = copy(
        consecutiveStimulations = consecutiveStimulations + other.consecutiveStimulations,
    )
}
