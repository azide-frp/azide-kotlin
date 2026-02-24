package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank_deprecated

@Suppress("ClassName")
data object ReactiveBag_generic_testUtils {
    data object SourceReactiveBagTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceBagChanges = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationScenarioBank_sourceBagChangesRevoked = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationScenarioBank_sourceBagChangesCorrected = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )
}
