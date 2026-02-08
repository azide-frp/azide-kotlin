package dev.azide.core.collections.reactive_list

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveList_generic_testUtils {
    data object SourceReactiveListTag : TestInputReactiveCollectionTag
    
    val stimulationBank_sourceEffectListChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationBank_sourceEffectListChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )

    val stimulationBank_sourceEffectListChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveListTag,
        ),
    )
}
