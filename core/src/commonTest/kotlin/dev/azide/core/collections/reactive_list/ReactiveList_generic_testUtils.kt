package dev.azide.core.collections.reactive_list

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveList_generic_testUtils {
    data object SourceReactiveListTag : TestInputReactiveCollectionTag
    
    val stimulationBank_sourceListChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationBank_sourceListChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationBank_sourceListChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )
}
