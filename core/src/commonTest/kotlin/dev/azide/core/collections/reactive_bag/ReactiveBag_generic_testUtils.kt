package dev.azide.core.collections.reactive_bag

import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank

@Suppress("ClassName")
data object ReactiveBag_generic_testUtils {
    data object SourceReactiveBagTag : TestInputReactiveCollectionTag

    val stimulationBank_sourceEffectBagChanges = TestStimulationBank.build(
        TestInputReactiveCollectionTag.changeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationBank_sourceEffectBagChangesRevoked = TestStimulationBank.build(
        TestInputReactiveCollectionTag.revokedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )

    val stimulationBank_sourceEffectBagChangesCorrected = TestStimulationBank.build(
        TestInputReactiveCollectionTag.correctedChangeScenario(
            inputReactiveCollectionTag = SourceReactiveBagTag,
        ),
    )
}
