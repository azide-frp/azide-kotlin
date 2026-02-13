package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object ReactiveBag_generic_testUtils {
    data object SourceReactiveBagTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceBagChanges = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationScenarioBank_sourceBagChangesRevoked = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationScenarioBank_sourceBagChangesCorrected = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )
}
