package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveBag_generic_testUtils {
    data object SourceReactiveBagTag : TestInputReactiveCollectionTag

    val stimulationBank_sourceBagChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationBank_sourceBagChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationBank_sourceBagChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )
}
