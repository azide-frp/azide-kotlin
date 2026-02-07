package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedListVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.abstract_vertices.AbstractStatefulTrackedListVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionStimulationTag
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import kotlin.test.assertTrue

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
                init {
                    require(index >= 0) {
                        "Insertion index must be non-negative."
                    }
                }

                override val indexRange: IntRange
                    get() = index until index

                override fun verifyIsApplicable(
                    targetList: List<ElementT>,
                ) {
                    assertTrue(
                        actual = index in 0..targetList.size,
                        message = "Insertion index $index is out of bounds for target list of size ${targetList.size}.",
                    )
                }

                override fun toListChangePart(): ListChange.Part<ElementT> = ListChange.Part(
                    firstIndexInclusive = index,
                    lastIndexExclusive = index,
                    newElements = newElements,
                )
            }

            data class Removal<ElementT>(
                override val indexRange: OpenEndRange<Int>,
            ) : Part<ElementT> {
                init {
                    require(indexRange.start >= 0) {
                        "Removal Part must have a non-negative start index."
                    }

                    require(!indexRange.isEmpty()) {
                        "Removal Part must have a non-empty indexRange."
                    }
                }

                override fun verifyIsApplicable(
                    targetList: List<ElementT>,
                ) {
                    assertTrue(
                        actual = indexRange.endExclusive <= targetList.size,
                        message = "Removal index range $indexRange is out of bounds for target list of size ${targetList.size}.",
                    )
                }

                override fun toListChangePart(): ListChange.Part<ElementT> = ListChange.Part(
                    firstIndexInclusive = indexRange.start,
                    lastIndexExclusive = indexRange.endExclusive,
                    newElements = emptyList(),
                )
            }

            data class Replacement<ElementT>(
                override val indexRange: OpenEndRange<Int>,
                val replacedElements: List<ElementT>,
            ) : Part<ElementT> {
                init {
                    require(indexRange.start >= 0) {
                        "Replacement Part must have a non-negative start index."
                    }

                    require(!indexRange.isEmpty()) {
                        "Replacement Part must have a non-empty indexRange."
                    }
                }

                override fun verifyIsApplicable(
                    targetList: List<ElementT>,
                ) {
                    assertTrue(
                        actual = indexRange.endExclusive <= targetList.size,
                        message = "Removal index range $indexRange is out of bounds for target list of size ${targetList.size}.",
                    )
                }

                override fun toListChangePart(): ListChange.Part<ElementT> = ListChange.Part(
                    firstIndexInclusive = indexRange.start,
                    lastIndexExclusive = indexRange.endExclusive,
                    newElements = replacedElements,
                )
            }

            val indexRange: OpenEndRange<Int>

            fun verifyIsApplicable(
                targetList: List<ElementT>,
            )

            fun toListChangePart(): ListChange.Part<ElementT>
        }

        companion object {
            fun <ElementT> of(vararg parts: Part<ElementT>): ChangeDescription<ElementT> =
                ChangeDescription(parts = parts.toList())
        }

        init {
            require(parts.isNotEmpty()) {
                "A ChangeDescription must have at least one part."
            }

            parts.zipWithNext().forEach { (part, nextPart) ->
                require(nextPart.indexRange.start >= part.indexRange.endExclusive) {
                    "Change parts must not overlap or be out of order. Part $part is followed by $nextPart."
                }
            }
        }

        fun toListChange(): ListChange<ElementT> = ListChange(
            parts = parts.map { it.toListChangePart() },
        )

        fun verifyIsApplicable(
            targetList: List<ElementT>,
        ) {
            parts.forEach { part ->
                part.verifyIsApplicable(
                    targetList = targetList,
                )
            }
        }
    }

    private val _vertex = object : AbstractStatefulTrackedListVertex<ElementT>(
        initialElements = initialElements.toMutableList(),
    ) {
        fun change(
            propagationContext: Transactions.PropagationContext,
            change: ListChange<ElementT>,
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
            correctedChange: ListChange<ElementT>,
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

    fun change(
        description: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            description.verifyIsApplicable(
                targetVertex = _vertex,
                propagationContext = propagationContext,
            )

            _vertex.change(
                propagationContext = propagationContext,
                change = description.toListChange(),
            )
        }
    }

    fun correctChange(
        correctedDescription: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            correctedDescription.verifyIsApplicable(
                targetVertex = _vertex,
                propagationContext = propagationContext,
            )

            _vertex.correctChange(
                propagationContext = propagationContext,
                correctedChange = correctedDescription.toListChange(),
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

    override val trackedVertex: TrackedListVertex<ElementT>
        get() = _vertex
}

fun <ElementT> ChangeDescription<ElementT>.verifyIsApplicable(
    targetVertex: TrackedListVertex<ElementT>,
    propagationContext: Transactions.PropagationContext,
) {
    val oldContentView: List<ElementT> = targetVertex.getOldContentView(
        propagationContext = propagationContext,
    )

    verifyIsApplicable(
        targetList = oldContentView,
    )
}

fun <ElementT> TestInputReactiveList<ElementT>.changing(
    tag: TestInputReactiveCollectionTag,
    description: ChangeDescription<ElementT>,
): TestStimulationMap = TestStimulationMap.of(
    TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ) to change(
        description = description,
    ),
)

fun <ElementT> TestInputReactiveList<ElementT>.revokingChange(
    description: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        description = description,
    ),
    secondStimulation = revokeChange(),
)

fun <ElementT> TestInputReactiveList<ElementT>.revokingChange(
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

fun <ElementT> TestInputReactiveList<ElementT>.correctingChange(
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
