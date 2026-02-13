package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object ReactiveSet_generic_testUtils {
    data object SourceReactiveSetTag : TestInputReactiveCollectionTag

    val stimulationScenarioBank_sourceSetChanges = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )

    val stimulationScenarioBank_sourceSetChangesRevoked = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )

    val stimulationScenarioBank_sourceSetChangesCorrected = TestStimulationScenarioBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )
}
