package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object ReactiveSet_generic_testUtils {
    data object SourceReactiveSetTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceSetChanges = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )

    val stimulationScenarioBank_sourceSetChangesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )

    val stimulationScenarioBank_sourceSetChangesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )
}
