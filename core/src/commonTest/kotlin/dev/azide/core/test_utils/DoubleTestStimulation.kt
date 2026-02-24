package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationTag_deprecated

data class DoubleTestStimulation(
    val firstStimulation: TestStimulation,
    val secondStimulation: TestStimulation,
) {
    fun tagged(
        firstTag: TestStimulationTag_deprecated,
        secondTag: TestStimulationTag_deprecated,
    ): TestStimulationMap = TestStimulationMap.of(
        firstTag to firstStimulation,
        secondTag to secondStimulation,
    )

    fun joint(): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            firstStimulation.stimulate(
                propagationContext = propagationContext,
            )

            secondStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
