package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.DynamicInterleavingUtils
import kotlin.random.Random

/**
 * A set of sequential test stimulations that are intended to be stimulated together, without specifying any particular
 * of execution (besides that stimulation from a single sequence should be stimulated in the order of its
 * [TestSequentialStimulation.consecutiveStimulations]).
 */
data class TestSequentialStimulationSet(
    val includedStimulations: Set<TestSequentialStimulation>,
) {
    fun determinizeArbitrarily(): TestSequentialStimulation = determinizeRandomly(
        random = Random(0),
    )

    fun determinizeRandomly(
        random: Random,
    ): TestSequentialStimulation = TestSequentialStimulation(
        consecutiveStimulations = DynamicInterleavingUtils.generateRandom(
            random = random,
            lists = includedStimulations.map { it.consecutiveStimulations },
        ),
    )
}
