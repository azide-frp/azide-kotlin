package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription

class TestInputReactiveList<ElementT>(
    initialElements: List<ElementT>,
) : ReactiveList<ElementT> {
    data class ChangeDescription<ElementT>(
        val parts: List<Part<ElementT>>,
    ) {
        sealed interface Part<ElementT> {
            data class Insertion<ElementT>(
                val index: Int,
                val newElements: List<ElementT>,
            ) : Part<ElementT> {
                override val indexRange: IntRange
                    get() = index until index
            }

            data class Removal<ElementT>(
                override val indexRange: IntRange,
            ) : Part<ElementT> {
                init {
                    require(!indexRange.isEmpty()) {
                        "Removal Part must have a non-empty indexRange."
                    }
                }
            }

            data class Replacement<ElementT>(
                override val indexRange: IntRange,
                val replacedElements: List<ElementT>,
            ) : Part<ElementT> {
                init {
                    require(!indexRange.isEmpty()) {
                        "Replacement Part must have a non-empty indexRange."
                    }
                }
            }

            val indexRange: IntRange
        }

        companion object {
            fun <ElementT> of(vararg parts: Part<ElementT>): ChangeDescription<ElementT> =
                ChangeDescription(parts = parts.toList())
        }

        init {
            parts.zipWithNext().forEach { (part, nextPart) ->
                require(nextPart.indexRange.first > part.indexRange.last) {
                    "Change parts must not overlap or be out of order. Part $part is followed by $nextPart."
                }
            }
        }
    }

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

    override val trackedVertex: TrackedCollectionVertex<ElementT>
        get() = TODO("Not yet implemented")
}

fun <ElementT> TestInputReactiveList<ElementT>.revokingChange(
    description: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        description,
    ),
    secondStimulation = revokeChange(),
)

fun <ElementT> TestInputReactiveList<ElementT>.correctingChange(
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
