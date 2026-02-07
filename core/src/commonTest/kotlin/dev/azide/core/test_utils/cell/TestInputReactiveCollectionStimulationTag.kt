package dev.azide.core.test_utils.cell

import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationTag

sealed interface TestInputReactiveCollectionStimulationTag : TestStimulationTag {
    data class Change(
        val inputTag: TestInputReactiveCollectionTag,
    ) : TestInputReactiveCollectionStimulationTag

    data class ChangeRevocation(
        val inputTag: TestInputReactiveCollectionTag,
    ) : TestInputReactiveCollectionStimulationTag

    data class ChangeCorrection(
        val inputTag: TestInputReactiveCollectionTag,
    ) : TestInputReactiveCollectionStimulationTag
}
