package dev.azide.core.test_utils

data class TestSequentialStimulation(
    val consecutiveStimulations: List<TestStimulation>,
) {
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

    init {
        require(consecutiveStimulations.isNotEmpty()) {
            "A TestSequentialStimulation cannot be empty."
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
}
