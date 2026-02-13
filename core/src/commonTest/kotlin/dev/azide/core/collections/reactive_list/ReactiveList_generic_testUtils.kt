package dev.azide.core.collections.reactive_list

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object ReactiveList_generic_testUtils {
    data object SourceReactiveListTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceListChanges = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationScenarioBank_sourceListChangesRevoked = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationScenarioBank_sourceListChangesCorrected = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )
}
