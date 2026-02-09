package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractStatefulTrackedSetVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionStimulationTag
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet.ChangeDescription
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap

class TestInputReactiveSet<ElementT>(
    initialElements: Set<ElementT>,
) : ReactiveSet<ElementT> {
    private val _vertex = object : AbstractStatefulTrackedSetVertex<ElementT>(
        initialElements = initialElements.toMutableSet(),
    ) {
        fun change(
            propagationContext: Transactions.PropagationContext,
            change: SetChange<ElementT>,
        ) {
            if (ongoingChange != null) {
                throw IllegalStateException("Another change is already ongoing")
            }

            exposeChangeNotifyingListeners(
                propagationContext = propagationContext,
                change = change,
            )
        }

        fun correctChange(
            propagationContext: Transactions.PropagationContext,
            correctedChange: SetChange<ElementT>,
        ) {
            if (ongoingChange == null) {
                throw IllegalStateException("No ongoing change to correct")
            }

            exposeChangeNotifyingListeners(
                propagationContext = propagationContext,
                change = correctedChange,
            )
        }

        fun revokeChange(
            propagationContext: Transactions.PropagationContext,
        ) {
            if (ongoingChange == null) {
                throw IllegalStateException("No ongoing change to revoke")
            }

            exposeChangeNotifyingListeners(
                propagationContext = propagationContext,
                change = null,
            )
        }
    }

    data class ChangeDescription<ElementT>(
        val addedElements: Set<ElementT> = emptySet(),
        val removedElements: Set<ElementT> = emptySet(),
    ) {
        init {
            val intersection = addedElements.intersect(removedElements)
            require(intersection.isEmpty()) {
                "Elements cannot be both added and removed in the same change: $intersection"
            }
        }

        fun toSetChange(): SetChange<ElementT> = SetChange(
            addedElements = addedElements,
            removedElements = removedElements,
        )

        fun verifyIsApplicable(
            targetSet: Set<ElementT>,
        ) {
            for (element in addedElements) {
                require(!targetSet.contains(element)) {
                    "Element $element is already present in the target set."
                }
            }

            for (element in removedElements) {
                require(targetSet.contains(element)) {
                    "Element $element is not present in the target set for removal."
                }
            }
        }
    }

    fun change(
        changeDescription: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            changeDescription.verifyIsApplicable(
                targetSet = _vertex.getOldContentView(
                    propagationContext = propagationContext,
                ),
            )

            _vertex.change(
                propagationContext = propagationContext,
                change = changeDescription.toSetChange(),
            )
        }
    }

    fun correctChange(
        correctedChangeDescription: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            correctedChangeDescription.verifyIsApplicable(
                targetSet = _vertex.getOldContentView(
                    propagationContext = propagationContext,
                ),
            )

            _vertex.correctChange(
                propagationContext = propagationContext,
                correctedChange = correctedChangeDescription.toSetChange(),
            )
        }
    }


    fun revokeChange(): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.revokeChange(
                propagationContext = propagationContext,
            )
        }
    }

    override val trackedVertex: TrackedSetVertex<ElementT>
        get() = _vertex
}

fun <ElementT> TestInputReactiveSet<ElementT>.changing(
    tag: TestInputReactiveCollectionTag,
    changeDescription: ChangeDescription<ElementT>,
): TestStimulationMap = TestStimulationMap.of(
    TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ) to change(
        changeDescription = changeDescription,
    ),
)

fun <ElementT> TestInputReactiveSet<ElementT>.revokingChange(
    temporaryChangeDescription: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        changeDescription = temporaryChangeDescription,
    ),
    secondStimulation = revokeChange(),
)

fun <ElementT> TestInputReactiveSet<ElementT>.revokingChange(
    tag: TestInputReactiveCollectionTag,
    temporaryChangeDescription: ChangeDescription<ElementT>,
): TestStimulationMap = revokingChange(
    temporaryChangeDescription,
).tagged(
    firstTag = TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ),
    secondTag = TestInputReactiveCollectionStimulationTag.ChangeRevocation(
        inputTag = tag,
    ),
)

fun <ElementT> TestInputReactiveSet<ElementT>.correctingChange(
    intermediateChangeDescription: ChangeDescription<ElementT>,
    correctedChangeDescription: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        changeDescription = intermediateChangeDescription,
    ),
    secondStimulation = correctChange(
        correctedChangeDescription = correctedChangeDescription,
    ),
)

fun <ElementT> TestInputReactiveSet<ElementT>.correctingChange(
    tag: TestInputReactiveCollectionTag,
    intermediateChangeDescription: ChangeDescription<ElementT>,
    correctedChangeDescription: ChangeDescription<ElementT>,
): TestStimulationMap = correctingChange(
    intermediateChangeDescription = intermediateChangeDescription,
    correctedChangeDescription = correctedChangeDescription,
).tagged(
    firstTag = TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ),
    secondTag = TestInputReactiveCollectionStimulationTag.ChangeCorrection(
        inputTag = tag,
    ),
)
