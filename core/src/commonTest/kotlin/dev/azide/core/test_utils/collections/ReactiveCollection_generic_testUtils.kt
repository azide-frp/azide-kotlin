package dev.azide.core.test_utils.collections

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object ReactiveCollection_generic_testUtils {
    data object SourceReactiveCollectionTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceCollectionChanges = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveCollectionTag,
        ),
    )

    val stimulationScenarioBank_sourceCollectionChangesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveCollectionTag,
        ),
    )

    val stimulationScenarioBank_sourceCollectionChangesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveCollectionTag,
        ),
    )
}
