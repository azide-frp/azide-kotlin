package dev.azide.core.collections.reactive_list

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank_deprecated

@Suppress("ClassName")
data object ReactiveList_generic_testUtils {
    data object SourceReactiveListTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceListChanges = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationScenarioBank_sourceListChangesRevoked = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationScenarioBank_sourceListChangesCorrected = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )
}
