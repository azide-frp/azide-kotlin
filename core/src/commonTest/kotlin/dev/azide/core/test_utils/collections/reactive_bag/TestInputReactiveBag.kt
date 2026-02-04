package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionStimulationTag
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap

class TestInputReactiveBag<ElementT>(
    initialTaggedContent: Map<InputTag, ElementT>,
) : ReactiveBag<ElementT> {
    interface InputTag

    data class ChangeDescription<ElementT>(
        val addedElementByTag: Map<InputTag, ElementT> = emptyMap(),
        val replacedElementByTag: Map<InputTag, ElementT> = emptyMap(),
        val removedTags: Set<InputTag> = emptySet(),
    )

    fun change(
        description: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            TODO()
        }
    }

    fun correctChange(
        correctedDescription: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            TODO()
        }
    }

    fun revokeChange(): TestStimulation {
        TODO()
    }

    override val trackedVertex: TrackedTaggedBagVertex<ElementT> = TODO()
}

fun <ElementT> TestInputReactiveBag<ElementT>.changing(
    tag: TestInputReactiveCollectionTag,
    description: ChangeDescription<ElementT>,
): TestStimulationMap = TestStimulationMap.of(
    TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ) to change(
        description = description,
    ),
)

fun <ElementT> TestInputReactiveBag<ElementT>.revokingChange(
    intermediateDescription: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        description = intermediateDescription,
    ),
    secondStimulation = revokeChange(),
)

fun <ElementT> TestInputReactiveBag<ElementT>.revokingChange(
    tag: TestInputReactiveCollectionTag,
    intermediateDescription: ChangeDescription<ElementT>,
): TestStimulationMap = revokingChange(
    intermediateDescription,
).tagged(
    firstTag = TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ),
    secondTag = TestInputReactiveCollectionStimulationTag.ChangeRevocation(
        inputTag = tag,
    ),
)

fun <ElementT> TestInputReactiveBag<ElementT>.correctingChange(
    intermediateDescription: ChangeDescription<ElementT>,
    correctedDescription: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        description = intermediateDescription,
    ),
    secondStimulation = correctChange(
        correctedDescription = correctedDescription,
    ),
)

fun <ElementT> TestInputReactiveBag<ElementT>.correctingChange(
    tag: TestInputReactiveCollectionTag,
    intermediateDescription: ChangeDescription<ElementT>,
    correctedDescription: ChangeDescription<ElementT>,
): TestStimulationMap = correctingChange(
    intermediateDescription = intermediateDescription,
    correctedDescription = correctedDescription,
).tagged(
    firstTag = TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ),
    secondTag = TestInputReactiveCollectionStimulationTag.ChangeCorrection(
        inputTag = tag,
    ),
)
