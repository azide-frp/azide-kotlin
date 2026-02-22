package dev.azide.core.impl.collections.reactive_list.operated_vertices

import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractStatelessTrackedListVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.registerBoundListener
import dev.kmpx.collections.SortedCollections.RankKind
import dev.kmpx.collections.maps.MutableSortedMap
import dev.kmpx.collections.maps.treeMapOf

class SortedUniquelyTrackedListVertex<ElementT, SortKeyT : Comparable<SortKeyT>>(
    private val sourceVertex: TrackedCollectionVertex<SortableValue<ElementT, SortKeyT>>,
) : AbstractStatelessTrackedListVertex<ElementT>(), BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

    private var elementBySortKey: MutableSortedMap<SortKeyT, ElementT>? = null

    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        exposeChangeNotifyingListeners(
            propagationContext = propagationContext,
            change = sourceVertex.ongoingChange?.let { sourceOngoingChange ->
                buildChange(
                    sourceOngoingChange = sourceOngoingChange,
                    propagationContext = propagationContext,
                )
            },
        )
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: ListenableVertex.ActivationMode,
    ): ListChange<ElementT>? {
        if (upstreamListenerHandle != null || elementBySortKey != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        val initialOldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        elementBySortKey = treeMapOf(
            *initialOldContentView.map {
                it.sortKey to it.value
            }.toTypedArray(),
        )

        return sourceVertex.ongoingChange?.let { sourceOngoingChange ->
            buildChange(sourceOngoingChange, propagationContext)
        }
    }

    override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null

        elementBySortKey = null
    }

    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): List<ElementT> = when (val foundSortedElements = elementBySortKey) {
        // Inactive vertex
        null -> {
            val sourceContentView = sourceVertex.getOldContentView(
                propagationContext = propagationContext,
            )

            sourceContentView.sortedBy { it.sortKey }.map { it.value }
        }

        // Active vertex
        else -> foundSortedElements.sortedValues
    }

    private fun buildChange(
        sourceOngoingChange: TrackedGenericCollectionVertex.CollectionChange<SortableValue<ElementT, SortKeyT>>,
        propagationContext: Transactions.PropagationContext,
    ): ListChange<ElementT> {
        val elementBySortKey =
            this.elementBySortKey ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        val changeBuilder = ChangeBuilder()

        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        val abolishedContentView = sourceOngoingChange.getAbolishedContentView(
            oldContentView = oldContentView,
        )

        abolishedContentView.forEach { removedSortableElement: SortableValue<ElementT, SortKeyT> ->
            val abolishedKeyRankResult = elementBySortKey.findKeyRank(removedSortableElement.sortKey)

            if (abolishedKeyRankResult.kind == RankKind.Potential) {
                throw IllegalStateException("Removed element with non-existing sort key found in current content")
            }

            changeBuilder.abolish(
                abolishedIndex = abolishedKeyRankResult.rank,
            )
        }

        sourceOngoingChange.introducedContentView.forEach { addedSortableElement: SortableValue<ElementT, SortKeyT> ->
            val introducedKeyRankResult = elementBySortKey.findKeyRank(addedSortableElement.sortKey)

            changeBuilder.introduce(
                predictedIndex = introducedKeyRankResult.rank,
                sortKey = addedSortableElement.sortKey,
                element = addedSortableElement.value,
            )
        }

        return changeBuilder.build()
    }

    private inner class ChangeBuilder {
        private val partBuilderByFirstIndexInclusive: MutableSortedMap<Int, PartBuilder> = treeMapOf()

        fun abolish(
            abolishedIndex: Int,
        ) {
            getOrCreatePartBuilderForIndex(abolishedIndex).addRemoval()
        }

        fun introduce(
            predictedIndex: Int,
            sortKey: SortKeyT,
            element: ElementT,
        ) {
            getOrCreatePartBuilderForIndex(predictedIndex).addAddition(
                sortKey = sortKey,
                element = element,
            )
        }

        fun build(): ListChange<ElementT> = ListChange(
            parts = partBuilderByFirstIndexInclusive.map { (_, partBuilder) ->
                partBuilder.build()
            },
        )

        /**
         * Finds the part builder covering the given index, or creates a new one if no such part builder exists.
         * The returned part builder must be updated to include the given index.
         */
        private fun getOrCreatePartBuilderForIndex(
            index: Int,
        ): PartBuilder {
            // Find the first part builder with index equal or smaller than the given index
            return partBuilderByFirstIndexInclusive.floorEntry(index)?.toPair()
                // ...but ignore it if it doesn't cover the given index
                ?.let { (_, partBuilder) ->
                    partBuilder.takeIf {
                        it.covers(
                            index = index,
                        )
                    }
                } ?: PartBuilder(
                firstIndexInclusive = index,
            ).also {  // if no suitable part builder is found, create a fresh one
                partBuilderByFirstIndexInclusive[index] = it
            }
        }
    }

    private inner class PartBuilder(
        val firstIndexInclusive: Int,
    ) {
        private var removedElementCount = 0

        val lastIndexExclusive: Int
            get() = firstIndexInclusive + removedElementCount

        private val newElementBySortKey: MutableSortedMap<SortKeyT, ElementT> = treeMapOf()

        fun addRemoval() {
            ++removedElementCount
        }

        fun addAddition(
            sortKey: SortKeyT,
            element: ElementT,
        ) {
            newElementBySortKey[sortKey] = element
        }

        fun build(): ListChange.Part<ElementT> = ListChange.Part(
            firstIndexInclusive = firstIndexInclusive,
            lastIndexExclusive = lastIndexExclusive,
            newElements = newElementBySortKey.sortedValues,
        )

        fun covers(
            index: Int,
        ): Boolean = index in firstIndexInclusive..lastIndexExclusive
    }
}
