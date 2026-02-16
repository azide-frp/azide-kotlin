package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.MutableTaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_bag.abstract_vertices.AbstractBaseStatefulTrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionStimulationTag
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag.ChangeDescription
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestInputReactiveBag<ElementT>(
    initialTaggedContent: Map<Tag, ElementT>,
) : ReactiveBag<ElementT> {
    constructor(
        initialTaggedElements: TaggedBag< ElementT>,
    ): this(initialTaggedContent = initialTaggedElements.elementByTag)

    data class ChangeDescription<ElementT>(
        val addedElementByTag: Map<Tag, ElementT> = emptyMap(),
        val replacedElementByTag: Map<Tag, ElementT> = emptyMap(),
        val removedTags: Set<Tag> = emptySet(),
    ) {
        companion object {
            fun <ElementT> of(
                addedElementByTag: Map<Tag, ElementT> = emptyMap(),
                replacedElementByTag: Map<Tag, ElementT> = emptyMap(),
                removedTags: Set<Tag> = emptySet(),
            ): ChangeDescription<ElementT>? {
                if (addedElementByTag.isEmpty() && replacedElementByTag.isEmpty() && removedTags.isEmpty()) {
                    return null
                }

                return ChangeDescription(
                    addedElementByTag = addedElementByTag,
                    replacedElementByTag = replacedElementByTag,
                    removedTags = removedTags,
                )
            }
        }

        init {
            require(addedElementByTag.isNotEmpty() || replacedElementByTag.isNotEmpty() || removedTags.isNotEmpty()) {
                "A change description must have at least one added, replaced, or removed element."
            }

            for (tag in addedElementByTag.keys) {
                require(tag !in replacedElementByTag.keys) {
                    "Tag $tag cannot be both added and replaced in the same change."
                }

                require(tag !in removedTags) {
                    "Tag $tag cannot be both added and removed in the same change."
                }
            }

            for (tag in replacedElementByTag.keys) {
                require(tag !in removedTags) {
                    "Tag $tag cannot be both replaced and removed in the same change."
                }
            }
        }

        fun toTaggedBagChange(): TaggedBagChange<ElementT> = TaggedBagChange(
            changedElementByTag = replacedElementByTag + addedElementByTag,
            removedTags = removedTags,
        )

        fun verifyIsApplicable(
            targetTaggedBag: TaggedBag<ElementT>,
        ) {
            for (tag: Tag in addedElementByTag.keys) {
                assertFalse(
                    actual = targetTaggedBag.containsTag(tag),
                    message = "Tag $tag is already present in the target tagged bag.",
                )
            }

            for (tag: Tag in replacedElementByTag.keys) {
                assertTrue(
                    actual = targetTaggedBag.containsTag(tag),
                    message = "Tag $tag is not present in the target tagged bag for replacement."
                )
            }

            for (tag: Tag in removedTags) {
                assertTrue(
                    actual = targetTaggedBag.containsTag(tag),
                    message = "Tag $tag is not present in the target tagged bag for removal."
                )
            }
        }
    }

    private val _vertex = object : AbstractBaseStatefulTrackedTaggedBagVertex<ElementT>(
        initialTaggedElements = MutableTaggedBag.ofTaggedContent(initialTaggedContent),
    ) {
        fun change(
            propagationContext: Transactions.PropagationContext,
            change: TaggedBagChange<ElementT>,
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
            correctedChange: TaggedBagChange<ElementT>,
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
        changeDescription: ChangeDescription<ElementT>,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            changeDescription.verifyIsApplicable(
                targetVertex = _vertex,
                propagationContext = propagationContext,
            )

            _vertex.change(
                propagationContext = propagationContext,
                change = changeDescription.toTaggedBagChange(),
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
                targetVertex = _vertex,
                propagationContext = propagationContext,
            )

            _vertex.correctChange(
                propagationContext = propagationContext,
                correctedChange = correctedChangeDescription.toTaggedBagChange(),
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

    override val trackedVertex: TrackedTaggedBagVertex<ElementT> = _vertex
}

fun <ElementT> ChangeDescription<ElementT>.verifyIsApplicable(
    targetVertex: TrackedTaggedBagVertex<ElementT>,
    propagationContext: Transactions.PropagationContext,
) {
    val oldContentView: TaggedBag<ElementT> = targetVertex.getOldContentView(
        propagationContext = propagationContext,
    )

    verifyIsApplicable(
        targetTaggedBag = oldContentView,
    )
}

fun <ElementT> TestInputReactiveBag<ElementT>.changing(
    tag: TestInputReactiveCollectionTag,
    changeDescription: ChangeDescription<ElementT>,
): TestStimulationMap = TestStimulationMap.of(
    TestInputReactiveCollectionStimulationTag.Change(
        inputTag = tag,
    ) to change(
        changeDescription = changeDescription,
    ),
)

fun <ElementT> TestInputReactiveBag<ElementT>.revokingChange(
    temporaryChangeDescription: ChangeDescription<ElementT>,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = change(
        changeDescription = temporaryChangeDescription,
    ),
    secondStimulation = revokeChange(),
)

fun <ElementT> TestInputReactiveBag<ElementT>.revokingChange(
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

fun <ElementT> TestInputReactiveBag<ElementT>.correctingChange(
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

fun <ElementT> TestInputReactiveBag<ElementT>.correctingChange(
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
