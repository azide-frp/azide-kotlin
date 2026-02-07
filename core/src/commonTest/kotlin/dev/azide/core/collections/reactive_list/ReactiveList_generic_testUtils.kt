package dev.azide.core.collections.reactive_list

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveList_generic_testUtils {
    data object SourceEffectReactiveListTag : TestInputReactiveCollectionTag
    
    val stimulationBank_sourceEffectListChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveListTag,
        ),
    )

    val stimulationBank_sourceEffectListChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveListTag,
        ),
    )

    val stimulationBank_sourceEffectListChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceEffectReactiveListTag,
        ),
    )
}
