package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveSet_generic_testUtils {
    data object SourceReactiveSetTag : TestInputReactiveCollectionTag

    val stimulationBank_sourceSetChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )

    val stimulationBank_sourceSetChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )

    val stimulationBank_sourceSetChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveSetTag,
        ),
    )
}
