package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractStatefulTrackedSetVertex
import dev.azide.core.test_utils.TestInputStimulation

class TestInputReactiveSet<ElementT>(
    initialElements: Set<ElementT>,
) : ReactiveSet<ElementT> {
    private val _vertex = object : AbstractStatefulTrackedSetVertex<ElementT>(
        initialElements = initialElements.toMutableSet(),
    ) {
        fun change(
            propagationContext: Transactions.PropagationContext,
            elementsToAdd: Set<ElementT>,
            elementsToRemove: Set<ElementT>,
        ) {
            if (ongoingChange != null) {
                throw IllegalStateException("Another change is already ongoing")
            }

            _change(
                propagationContext = propagationContext,
                elementsToAdd = elementsToAdd,
                elementsToRemove = elementsToRemove,
            )
        }

        fun correctChange(
            propagationContext: Transactions.PropagationContext,
            correctedElementsToAdd: Set<ElementT>,
            correctedElementsToRemove: Set<ElementT>,
        ) {
            if (ongoingChange == null) {
                throw IllegalStateException("No ongoing change to correct")
            }

            _change(
                propagationContext = propagationContext,
                elementsToAdd = correctedElementsToAdd,
                elementsToRemove = correctedElementsToRemove,
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

        private fun _change(
            propagationContext: Transactions.PropagationContext,
            elementsToAdd: Set<ElementT>,
            elementsToRemove: Set<ElementT>,
        ) {
            val intersection = elementsToAdd.intersect(elementsToRemove)

            if (intersection.isNotEmpty()) {
                throw IllegalArgumentException("Elements cannot be both added and removed: $intersection")
            }

            val oldContentView = getOldContentView(
                propagationContext = propagationContext,
            )

            if (elementsToAdd.any { oldContentView.contains(it) }) {
                throw IllegalArgumentException("New elements contain elements already present in the set")
            }

            if (elementsToRemove.any { !oldContentView.contains(it) }) {
                throw IllegalArgumentException("Elements to remove contain elements not present in the set")
            }

            exposeChangeNotifyingListeners(
                propagationContext = propagationContext,
                change = SetChange(
                    addedElements = elementsToAdd,
                    removedElements = elementsToRemove,
                ),
            )
        }
    }

    fun change(
        elementsToAdd: Set<ElementT>,
        elementsToRemove: Set<ElementT>,
    ): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.change(
                propagationContext = propagationContext,
                elementsToAdd = elementsToAdd,
                elementsToRemove = elementsToRemove,
            )
        }
    }

    fun correctChange(
        correctedElementsToAdd: Set<ElementT>,
        correctedElementsToRemove: Set<ElementT>,
    ): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.correctChange(
                propagationContext = propagationContext,
                correctedElementsToAdd = correctedElementsToAdd,
                correctedElementsToRemove = correctedElementsToRemove,
            )
        }
    }

    fun revokeChange(): TestInputStimulation = object : TestInputStimulation {
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

fun <ElementT> TestInputReactiveSet<ElementT>.revokingChange(
    elementsToAdd: Set<ElementT>,
    elementsToRemove: Set<ElementT>,
): TestInputStimulation = TestInputStimulation.combine(
    change(
        elementsToAdd = elementsToAdd,
        elementsToRemove = elementsToRemove,
    ),
    revokeChange(),
)

fun <ElementT> TestInputReactiveSet<ElementT>.correctingChange(
    intermediateElementsToAdd: Set<ElementT>,
    intermediateElementsToRemove: Set<ElementT>,
    correctedElementsToAdd: Set<ElementT>,
    correctedElementsToRemove: Set<ElementT>,
) = TestInputStimulation.combine(
    change(
        elementsToAdd = intermediateElementsToAdd,
        elementsToRemove = intermediateElementsToRemove,
    ),
    correctChange(
        correctedElementsToAdd = correctedElementsToAdd,
        correctedElementsToRemove = correctedElementsToRemove,
    ),
)
