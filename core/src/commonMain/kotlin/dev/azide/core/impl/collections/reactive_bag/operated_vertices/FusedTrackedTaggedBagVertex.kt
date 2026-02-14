package dev.azide.core.impl.collections.reactive_bag.operated_vertices

import dev.azide.core.Cell
import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.getNewValue
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractStatelessTrackedTaggedBagVertex
import dev.azide.core.impl.registerBoundListener
import dev.azide.core.impl.registerBoundListenerOnline
import kotlin.collections.component2

class FusedTrackedTaggedBagVertex<ElementT>(
    private val outerSourceBagVertex: TrackedTaggedBagVertex<Cell<ElementT>>,
) : AbstractStatelessTrackedTaggedBagVertex<ElementT>(), BoundListener {
    private inner class InnerCellListenerEntry(
        var cellVertex: CellVertex<ElementT>,
        var listenerHandle: ListenerHandle,
    )

    private inner class InnerCellListener(
        private val tag: Tag,
    ) : BoundListener {
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ) {
            val stableInnerSourceCellVertexByTag = this@FusedTrackedTaggedBagVertex.stableInnerSourceCellVertexByTag
                ?: throw IllegalStateException("Vertex doesn't seem to be active")

            val updatedUntouchedStableInnerCellVertexTags =
                this@FusedTrackedTaggedBagVertex.updatedUntouchedStableInnerCellVertexTags
                    ?: throw IllegalStateException("Vertex doesn't seem to be active")

            val changedInnerCellVertexByTag = this@FusedTrackedTaggedBagVertex.changedInnerCellVertexByTag

            val listenerEntry = upstreamNewInnerCellListenerEntryByTag?.get(tag)
                ?: throw IllegalStateException("Inner listener entry not found for tag: $tag")

            val cellVertex = listenerEntry.cellVertex

            val cellOngoingUpdate = cellVertex.ongoingUpdate

            when (cellOngoingUpdate) {
                null -> { // Update revocation
                    updatedUntouchedStableInnerCellVertexTags.remove(tag)
                }

                else -> { // Initial update / update correction
                    if (changedInnerCellVertexByTag == null || tag !in changedInnerCellVertexByTag) { // This inner cell vertex is stable (untouched)
                        if (tag !in stableInnerSourceCellVertexByTag) {
                            throw IllegalStateException("Stable cell vertex not found for tag: $tag")
                        }

                        // Ensure that it's marked that this stable inner updates
                        updatedUntouchedStableInnerCellVertexTags.add(tag)
                    }
                }
            }

            val builtChange = buildChange(
                propagationContext = propagationContext,
            )

            when (builtChange) {
                null -> {
                    if (ongoingChange != null) {
                        exposeChangeNotifyingListeners(
                            propagationContext = propagationContext,
                            change = null,
                        )
                    }
                }

                else -> {
                    exposeChangeNotifyingListeners(
                        propagationContext = propagationContext,
                        change = builtChange,
                    )
                }
            }
        }
    }

    /**
     * Listener handle for the outer source bag vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A handle to the listener registered in [outerSourceBagVertex].
     */
    private var upstreamSourceListenerHandle: ListenerHandle? = null

    /**
     * Map of the stable inner cell vertices.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A map from tags to respective current stable inner cell vertices.
     */
    private var stableInnerSourceCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>>? = null

    /**
     * Set of the tags of inner source cell vertices which updated in this transaction, as long as they are _untouched_
     * (i.e. the outer source bag vertex doesn't have an ongoing change that changes these tags or removes their tag).
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: Set with the tags of all untouched inner source cell vertices with an ongoing update.
     */
    private var updatedUntouchedStableInnerCellVertexTags: MutableSet<Tag>? = null

    /**
     * Map of the changed inner cell vertices.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active:
     * - If the [outerSourceBagVertex] has an ongoing change: A map from tags to respective changed inner cell vertices.
     * - Otherwise: `null`
     */
    private var changedInnerCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>>? = null

    /**
     * Set of the removed inner cell vertices tags.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active:
     * - If the [outerSourceBagVertex] has an ongoing change: A set of removed tags.
     * - Otherwise: `null`
     */
    private var removedInnerCellVertexTags: MutableSet<Tag>? = null

    /**
     * Map of the new inner cell listener entries. For each tag, the _new_ inner cell vertex is the changed inner cell
     * vertex (if the outer source bag has an ongoing change that changes this tag) or a respective stable inner cell
     * vertex otherwise.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A map from tags to inner cell listener entries. For each tag:
     * - If [changedInnerCellVertexByTag] contains this tag (if the outer source bags has an ongoing change that changes
     *   this tag), the listener is registered in the respective changed inner cell vertex.
     * - Otherwise: The listener is registered in the respective stable inner cell vertex from
     *   [stableInnerSourceCellVertexByTag].
     */
    private var upstreamNewInnerCellListenerEntryByTag: MutableMap<Tag, InnerCellListenerEntry>? = null

    /**
     * Handle the change of the source bag vertex.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        val stableInnerSourceCellVertexByTag =
            this.stableInnerSourceCellVertexByTag ?: throw IllegalStateException("Vertex doesn't seem to be active")

        val updatedUntouchedStableInnerCellVertexTags = this.updatedUntouchedStableInnerCellVertexTags
            ?: throw IllegalStateException("Vertex doesn't seem to be active")

        val changedInnerCellVertexByTag = this.changedInnerCellVertexByTag

        val removedInnerCellVertexTags = this.removedInnerCellVertexTags

        val upstreamNewInnerCellListenerEntryByTag = this.upstreamNewInnerCellListenerEntryByTag
            ?: throw IllegalStateException("Vertex doesn't seem to be active")

        val sourceBagOngoingChange = outerSourceBagVertex.ongoingChange

        when (sourceBagOngoingChange) {
            null -> { // Source bag change revoked
                processSourceBagRevokedChange(
                    stableInnerSourceCellVertexByTag = stableInnerSourceCellVertexByTag,
                    updatedUntouchedStableInnerCellVertexTags = updatedUntouchedStableInnerCellVertexTags,
                    changedInnerCellVertexByTag = changedInnerCellVertexByTag,
                    removedInnerCellVertexTags = removedInnerCellVertexTags,
                    upstreamNewInnerCellListenerEntryByTag = upstreamNewInnerCellListenerEntryByTag,
                    propagationContext = propagationContext,
                )
            }

            else -> { // Source bag new change / change correction
                processSourceBagProperChange(
                    stableInnerSourceCellVertexByTag = stableInnerSourceCellVertexByTag,
                    updatedUntouchedStableInnerCellVertexTags = updatedUntouchedStableInnerCellVertexTags,
                    changedInnerCellVertexByTag = changedInnerCellVertexByTag,
                    removedInnerCellVertexTags = removedInnerCellVertexTags,
                    upstreamNewInnerCellListenerEntryByTag = upstreamNewInnerCellListenerEntryByTag,
                    propagationContext = propagationContext,
                    sourceBagOngoingChange = sourceBagOngoingChange,
                )
            }
        }

        val builtChange = buildChange(
            propagationContext = propagationContext,
        )

        when (builtChange) {
            null -> {
                if (ongoingChange != null) {
                    exposeChangeNotifyingListeners(
                        propagationContext = propagationContext,
                        change = null,
                    )
                }
            }

            else -> {
                exposeChangeNotifyingListeners(
                    propagationContext = propagationContext,
                    change = builtChange,
                )
            }
        }
    }

    private fun processSourceBagRevokedChange(
        stableInnerSourceCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>>,
        updatedUntouchedStableInnerCellVertexTags: MutableSet<Tag>,
        changedInnerCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>>?,
        removedInnerCellVertexTags: MutableSet<Tag>?,
        upstreamNewInnerCellListenerEntryByTag: MutableMap<Tag, InnerCellListenerEntry>,
        propagationContext: Transactions.PropagationContext,
    ) {
        // Unregister the listeners for all previously changed inner cell vertices
        changedInnerCellVertexByTag?.entries?.forEach { (previouslyChangedTag, previousChangedInnerCellVertex) ->
            val previouslyReplacedCellVertex: CellVertex<ElementT>? =
                stableInnerSourceCellVertexByTag[previouslyChangedTag]

            when (previouslyReplacedCellVertex) {
                null -> { // Addition revocation
                    val previouslyAddedListenerEntry =
                        upstreamNewInnerCellListenerEntryByTag.remove(previouslyChangedTag)
                            ?: throw IllegalStateException("Inner listener entry not found for tag")

                    val previousAddedListenedCellVertex = previouslyAddedListenerEntry.cellVertex
                    val previousAddedListenerHandle = previouslyAddedListenerEntry.listenerHandle

                    if (previousAddedListenedCellVertex != previousChangedInnerCellVertex) {
                        throw IllegalStateException("Inconsistent added cell vertex for tag $previouslyChangedTag")
                    }

                    previousAddedListenedCellVertex.unregisterListener(
                        handle = previousAddedListenerHandle,
                    )
                }

                else -> { // Replacement revocation
                    val changedListenerEntry = upstreamNewInnerCellListenerEntryByTag[previouslyChangedTag]
                        ?: throw IllegalStateException("Inner listener entry not found for tag")

                    // Unregister listener from previous replacing cell vertex

                    val previousReplacingListenedCellVertex = changedListenerEntry.cellVertex
                    val previousReplacingListenerHandle = changedListenerEntry.listenerHandle

                    if (previousReplacingListenedCellVertex != previousChangedInnerCellVertex) {
                        throw IllegalStateException("Inconsistent replaced cell vertex for tag $previouslyChangedTag")
                    }

                    // FIXME: Untested expression
                    previousChangedInnerCellVertex.unregisterListener(
                        handle = previousReplacingListenerHandle,
                    )

                    // Reregister the listener for the stable inner cell vertex
                    val fallbackListenerHandle = previouslyReplacedCellVertex.registerBoundListenerOnline(
                        propagationContext = propagationContext,
                        listener = InnerCellListener(tag = previouslyChangedTag),
                    )

                    changedListenerEntry.cellVertex = previouslyReplacedCellVertex
                    changedListenerEntry.listenerHandle = fallbackListenerHandle

                    if (previouslyReplacedCellVertex.ongoingUpdate != null) {
                        val wasAdded = updatedUntouchedStableInnerCellVertexTags.add(previouslyChangedTag)

                        if (!wasAdded) {
                            throw IllegalStateException("Tag $previouslyChangedTag was already marked as updated in stable inner cell vertices")
                        }
                    }
                }
            }
        }

        removedInnerCellVertexTags?.forEach { previouslyRemovedTag -> // Removal revocation
            val previouslyRemovedCellVertex = stableInnerSourceCellVertexByTag[previouslyRemovedTag]
                ?: throw IllegalStateException("Stable inner cell vertex not found for tag")

            val fallbackListenerHandle = previouslyRemovedCellVertex.registerBoundListenerOnline(
                propagationContext = propagationContext,
                listener = InnerCellListener(tag = previouslyRemovedTag),
            )

            val previousListenerEntry = upstreamNewInnerCellListenerEntryByTag.put(
                key = previouslyRemovedTag,
                value = InnerCellListenerEntry(
                    cellVertex = previouslyRemovedCellVertex,
                    listenerHandle = fallbackListenerHandle,
                ),
            )

            if (previousListenerEntry != null) {
                throw IllegalStateException("Listener entry already exists for tag $previouslyRemovedTag")
            }

            if (previouslyRemovedCellVertex.ongoingUpdate != null) {
                val wasAdded = updatedUntouchedStableInnerCellVertexTags.add(previouslyRemovedTag)

                if (!wasAdded) {
                    throw IllegalStateException("Tag $previouslyRemovedTag was already marked as updated in stable inner cell vertices")
                }
            }
        }

        this.changedInnerCellVertexByTag = null
        this.removedInnerCellVertexTags = null
    }

    private fun processSourceBagProperChange(
        stableInnerSourceCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>>,
        updatedUntouchedStableInnerCellVertexTags: MutableSet<Tag>,
        changedInnerCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>>?,
        removedInnerCellVertexTags: MutableSet<Tag>?,
        upstreamNewInnerCellListenerEntryByTag: MutableMap<Tag, InnerCellListenerEntry>,
        propagationContext: Transactions.PropagationContext,
        sourceBagOngoingChange: TaggedBagChange<Cell<ElementT>>,
    ) {
        val newChangedInnerCellVertexByTag =
            sourceBagOngoingChange.changedElementByTag.mapValuesTo(mutableMapOf()) { (newlyChangedTag: Tag, newlyChangedCell: Cell<ElementT>) ->
                val replacedCellVertex = stableInnerSourceCellVertexByTag[newlyChangedTag]
                val previouslyChangedCellVertex = changedInnerCellVertexByTag?.get(newlyChangedTag)

                when (previouslyChangedCellVertex) {
                    null -> { // New change (addition/replacement)
                        when (replacedCellVertex) {
                            null -> { // Addition (new)
                                val addedInnerCellVertex = newlyChangedCell.vertex

                                // Register listener to the newly added inner cell vertex
                                val addedListenerHandle = addedInnerCellVertex.registerBoundListenerOnline(
                                    propagationContext = propagationContext,
                                    listener = InnerCellListener(tag = newlyChangedTag),
                                )

                                val previousListenerEntry = upstreamNewInnerCellListenerEntryByTag.put(
                                    key = newlyChangedTag,
                                    value = InnerCellListenerEntry(
                                        cellVertex = addedInnerCellVertex,
                                        listenerHandle = addedListenerHandle,
                                    ),
                                )

                                if (previousListenerEntry != null) {
                                    throw IllegalStateException("Listener entry already exists for added tag $newlyChangedTag")
                                }

                                addedInnerCellVertex
                            }

                            else -> { // Replacement (new)
                                val replacingInnerCellVertex = newlyChangedCell.vertex

                                val changedListenerEntry = upstreamNewInnerCellListenerEntryByTag[newlyChangedTag]
                                    ?: throw IllegalStateException("Inner listener entry not found for tag: $newlyChangedTag")

                                // Unregister listener from the replaced stable inner cell vertex

                                val replacedListenedCellVertex = changedListenerEntry.cellVertex
                                val replacedListenerHandle = changedListenerEntry.listenerHandle

                                if (replacedListenedCellVertex != replacedCellVertex) {
                                    throw IllegalStateException("Inconsistent replaced cell vertex for tag $newlyChangedTag")
                                }

                                // FIXME: Untested expression
                                replacedListenedCellVertex.unregisterListener(
                                    handle = replacedListenerHandle,
                                )

                                // Ensure that the replaced stable inner cell vertex is not marked as updated
                                updatedUntouchedStableInnerCellVertexTags.remove(newlyChangedTag)

                                // Register listener to the newly replacing inner cell vertex

                                val replacingListenerHandle = replacingInnerCellVertex.registerBoundListenerOnline(
                                    propagationContext = propagationContext,
                                    listener = InnerCellListener(tag = newlyChangedTag),
                                )

                                changedListenerEntry.cellVertex = replacingInnerCellVertex
                                changedListenerEntry.listenerHandle = replacingListenerHandle

                                replacingInnerCellVertex
                            }
                        }
                    }

                    else -> { // Corrected change (addition/replacement)
                        when (replacedCellVertex) {
                            null -> { // Addition (corrected)
                                val previouslyAddedCellVertex = previouslyChangedCellVertex
                                val newAddedCellVertex = newlyChangedCell.vertex

                                val changedListenerEntry = upstreamNewInnerCellListenerEntryByTag[newlyChangedTag]
                                    ?: throw IllegalStateException("Inner listener entry not found for tag: $newlyChangedTag")

                                // Unregister listener from previous added cell vertex

                                val previousAddedListenedCellVertex = changedListenerEntry.cellVertex
                                val previousAddedListenerHandle = changedListenerEntry.listenerHandle

                                if (previousAddedListenedCellVertex != previouslyAddedCellVertex) {
                                    throw IllegalStateException("Inconsistent added cell vertex for tag $newlyChangedTag")
                                }

                                // FIXME: Untested expression
                                previousAddedListenedCellVertex.unregisterListener(
                                    handle = previousAddedListenerHandle,
                                )

                                // Register listener to new added cell vertex

                                val newAddedListenerHandle = newAddedCellVertex.registerBoundListenerOnline(
                                    propagationContext = propagationContext,
                                    listener = InnerCellListener(tag = newlyChangedTag),
                                )

                                changedListenerEntry.cellVertex = newAddedCellVertex
                                changedListenerEntry.listenerHandle = newAddedListenerHandle

                                newAddedCellVertex
                            }

                            else -> { // Replacement (corrected)
                                val previouslyReplacingCellVertex = previouslyChangedCellVertex
                                val newReplacingCellVertex = newlyChangedCell.vertex

                                val changedListenerEntry = upstreamNewInnerCellListenerEntryByTag[newlyChangedTag]
                                    ?: throw IllegalStateException("Inner listener entry not found for tag: $newlyChangedTag")

                                // Unregister listener from previous replacing cell vertex

                                val previousReplacingListenedCellVertex = changedListenerEntry.cellVertex
                                val previousReplacingListenerHandle = changedListenerEntry.listenerHandle

                                if (previousReplacingListenedCellVertex != previouslyReplacingCellVertex) {
                                    throw IllegalStateException("Inconsistent replaced cell vertex for tag $newlyChangedTag")
                                }

                                // FIXME: Untested expression
                                previouslyReplacingCellVertex.unregisterListener(
                                    handle = previousReplacingListenerHandle,
                                )

                                // Register listener to new replacing cell vertex

                                val newReplacingListenerHandle = newReplacingCellVertex.registerBoundListenerOnline(
                                    propagationContext = propagationContext,
                                    listener = InnerCellListener(tag = newlyChangedTag),
                                )

                                changedListenerEntry.cellVertex = newReplacingCellVertex
                                changedListenerEntry.listenerHandle = newReplacingListenerHandle

                                newReplacingCellVertex
                            }
                        }
                    }
                }
            }

        changedInnerCellVertexByTag?.forEach { (previouslyChangedTag, previousChangedCellVertex) ->
            if (previouslyChangedTag !in sourceBagOngoingChange.changedElementByTag) { // Un-addition / un-replacement
                val previouslyReplacedCellVertex = stableInnerSourceCellVertexByTag[previouslyChangedTag]

                when (previouslyReplacedCellVertex) {
                    null -> { // Un-addition
                        val previousAddedCellVertex = previousChangedCellVertex

                        val previouslyAddedListenerEntry =
                            upstreamNewInnerCellListenerEntryByTag.remove(previouslyChangedTag)
                                ?: throw IllegalStateException("Inner listener entry not found for tag: $previouslyChangedTag")

                        // Unregister listener from the previously added cell vertex

                        val previousAddedListenedCellVertex = previouslyAddedListenerEntry.cellVertex
                        val previousAddedListenerHandle = previouslyAddedListenerEntry.listenerHandle

                        if (previousAddedListenedCellVertex != previousAddedCellVertex) {
                            throw IllegalStateException("Inconsistent added cell vertex for tag $previouslyChangedTag")
                        }

                        // FIXME: Untested expression
                        previousAddedListenedCellVertex.unregisterListener(
                            handle = previousAddedListenerHandle,
                        )
                    }

                    else -> { // Un-replacement
                        val previousReplacingCellVertex = previousChangedCellVertex

                        val changedListenerEntry =
                            upstreamNewInnerCellListenerEntryByTag[previouslyChangedTag] ?: throw IllegalStateException(
                                "Inner listener entry not found for tag: $previouslyChangedTag"
                            )

                        // Unregister listener from previous replacing cell vertex

                        val previousReplacingListenedCellVertex = changedListenerEntry.cellVertex
                        val previousReplacingListenerHandle = changedListenerEntry.listenerHandle

                        if (previousReplacingListenedCellVertex != previousReplacingCellVertex) {
                            throw IllegalStateException("Inconsistent added cell vertex for tag $previouslyChangedTag")
                        }

                        // FIXME: Untested expression
                        previousReplacingListenedCellVertex.unregisterListener(
                            handle = previousReplacingListenerHandle,
                        )

                        // Reregister listener to the previously replaced stable inner cell vertex

                        val fallbackListenerHandle = previouslyReplacedCellVertex.registerBoundListenerOnline(
                            propagationContext = propagationContext,
                            listener = InnerCellListener(tag = previouslyChangedTag),
                        )

                        changedListenerEntry.cellVertex = previouslyReplacedCellVertex
                        changedListenerEntry.listenerHandle = fallbackListenerHandle

                        if (previouslyReplacedCellVertex.ongoingUpdate != null) {
                            val wasAdded = updatedUntouchedStableInnerCellVertexTags.add(previouslyChangedTag)

                            if (!wasAdded) {
                                throw IllegalStateException("Tag $previouslyChangedTag was already marked as updated in stable inner cell vertices")
                            }
                        }
                    }
                }
            }
        }

        this@FusedTrackedTaggedBagVertex.changedInnerCellVertexByTag = newChangedInnerCellVertexByTag

        // Unregister listeners for all newly removed inner cell vertices
        sourceBagOngoingChange.removedTags.forEach { newlyRemovedTag ->
            if (removedInnerCellVertexTags == null || newlyRemovedTag !in removedInnerCellVertexTags) {
                // If the tag wasn't previously removed, unregister the listener for the respective stable inner
                // cell vertex

                val newlyRemovedCellVertex = stableInnerSourceCellVertexByTag[newlyRemovedTag]
                    ?: throw IllegalStateException("Stable inner cell vertex not found for tag")

                val newlyRemovedListenerEntry = upstreamNewInnerCellListenerEntryByTag.remove(newlyRemovedTag)
                    ?: throw IllegalStateException("Stable inner cell vertex not found for tag")

                val newlyRemovedListenedCellVertex = newlyRemovedListenerEntry.cellVertex
                val newlyRemovedListenerHandle = newlyRemovedListenerEntry.listenerHandle

                if (newlyRemovedListenedCellVertex != newlyRemovedCellVertex) {
                    throw IllegalStateException("Inconsistent removed cell vertex for tag $newlyRemovedTag")
                }

                newlyRemovedListenedCellVertex.unregisterListener(
                    handle = newlyRemovedListenerHandle,
                )

                updatedUntouchedStableInnerCellVertexTags.remove(newlyRemovedTag)
            }
        }

        removedInnerCellVertexTags?.forEach { previouslyRemovedTag ->
            if (previouslyRemovedTag !in sourceBagOngoingChange.removedTags) { // Un-removal
                // If the tag is not removed in the current revision of the source bag change, re-register the
                // listener for the respective stable inner cell vertex.

                val previouslyRemovedCellVertex = stableInnerSourceCellVertexByTag[previouslyRemovedTag]
                    ?: throw IllegalStateException("Stable inner cell vertex not found for tag")

                val fallbackListenerHandle = previouslyRemovedCellVertex.registerBoundListenerOnline(
                    propagationContext = propagationContext,
                    listener = InnerCellListener(tag = previouslyRemovedTag),
                )

                val previousListenerEntry = upstreamNewInnerCellListenerEntryByTag.put(
                    key = previouslyRemovedTag,
                    value = InnerCellListenerEntry(
                        cellVertex = previouslyRemovedCellVertex,
                        listenerHandle = fallbackListenerHandle,
                    ),
                )

                if (previousListenerEntry != null) {
                    throw IllegalStateException("Listener entry already exists for tag $previouslyRemovedTag")
                }

                if (previouslyRemovedCellVertex.ongoingUpdate != null) {
                    val wasAdded = updatedUntouchedStableInnerCellVertexTags.add(previouslyRemovedTag)

                    if (!wasAdded) {
                        throw IllegalStateException("Tag $previouslyRemovedTag was already marked as updated in stable inner cell vertices")
                    }
                }
            }
        }

        this.removedInnerCellVertexTags = sourceBagOngoingChange.removedTags.toMutableSet()
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): TaggedBagChange<ElementT>? {
        if (upstreamSourceListenerHandle != null || upstreamNewInnerCellListenerEntryByTag != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        // Register listener on source bag vertex
        this.upstreamSourceListenerHandle = outerSourceBagVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        val initialTaggedInnerCells = outerSourceBagVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        val initialStableInnerSourceCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>> =
            initialTaggedInnerCells.elementByTag.mapValuesTo(mutableMapOf()) { (_, initialInnerCell) ->
                initialInnerCell.vertex
            }

        this.stableInnerSourceCellVertexByTag = initialStableInnerSourceCellVertexByTag

        val initialSourceBagOngoingChange: TaggedBagChange<Cell<ElementT>>? = outerSourceBagVertex.ongoingChange

        return when (initialSourceBagOngoingChange) {
            null -> { // The outer source bag doesn't have an ongoing change
                // Register listeners for all stable inner cells and collect their updates
                this.upstreamNewInnerCellListenerEntryByTag =
                    initialStableInnerSourceCellVertexByTag.entries.associateTo(mutableMapOf()) { (stableTag, initialStableInnerCellVertex) ->
                        val innerCellListenerHandle = initialStableInnerCellVertex.registerBoundListener(
                            propagationContext = propagationContext,
                            listener = InnerCellListener(tag = stableTag),
                            mode = mode,
                        )

                        stableTag to InnerCellListenerEntry(
                            cellVertex = initialStableInnerCellVertex,
                            listenerHandle = innerCellListenerHandle,
                        )
                    }

                val initialStableInnerSourceCellUpdatedValueByTag: Map<Tag, ElementT> =
                    initialStableInnerSourceCellVertexByTag.entries.mapNotNull { (stableTag, initialStableInnerCellVertex) ->
                        initialStableInnerCellVertex.ongoingUpdate?.let { stableTag to it.updatedValue }
                    }.toMap()

                this.updatedUntouchedStableInnerCellVertexTags =
                    initialStableInnerSourceCellUpdatedValueByTag.keys.toMutableSet()

                this.changedInnerCellVertexByTag = null
                this.removedInnerCellVertexTags = null

                when {
                    // None of the inner cells have an ongoing update - no fused change
                    initialStableInnerSourceCellUpdatedValueByTag.isEmpty() -> null

                    // Some inner cells have ongoing updates - build fused change
                    else -> TaggedBagChange(
                        changedElementByTag = initialStableInnerSourceCellUpdatedValueByTag,
                        removedTags = emptySet(),
                    )
                }
            }

            else -> { // The outer source bag has an ongoing change
                val initialUntouchedInnerSourceCellTaggedEntrySequence: Sequence<MutableMap.MutableEntry<Tag, CellVertex<ElementT>>> =
                    initialStableInnerSourceCellVertexByTag.entries.asSequence().filter { (stableTag, _) ->
                        when (stableTag) {
                            // This stable inner cell vertex is being replaced - do nothing
                            in initialSourceBagOngoingChange.changedElementByTag -> false

                            // This stable inner cell vertex is being removed - do nothing
                            in initialSourceBagOngoingChange.removedTags -> false

                            // This stable inner cell vertex is currently untouched - register listener and process update
                            else -> true
                        }
                    }

                val initialUpstreamNewInnerCellListenerEntryByTag = mutableMapOf<Tag, InnerCellListenerEntry>()

                this.upstreamNewInnerCellListenerEntryByTag = initialUpstreamNewInnerCellListenerEntryByTag

                // Register listeners for all untouched stable inner cells
                initialUntouchedInnerSourceCellTaggedEntrySequence.associateTo(initialUpstreamNewInnerCellListenerEntryByTag) { (stableTag, initialStableInnerCellVertex) ->
                    val listenerHandle = initialStableInnerCellVertex.registerBoundListener(
                        propagationContext = propagationContext,
                        listener = InnerCellListener(tag = stableTag),
                        mode = mode,
                    )

                    stableTag to InnerCellListenerEntry(
                        cellVertex = initialStableInnerCellVertex,
                        listenerHandle = listenerHandle,
                    )
                }

                // Collect the updated values of all untouched stable inner cell vertices
                val initialUntouchedInnerSourceCellUpdatedValueByTag: Map<Tag, ElementT> =
                    initialUntouchedInnerSourceCellTaggedEntrySequence.mapNotNull { (stableTag, initialStableInnerCellVertex) ->
                        initialStableInnerCellVertex.ongoingUpdate?.let { stableTag to it.updatedValue }
                    }.toMap()

                this.updatedUntouchedStableInnerCellVertexTags =
                    initialUntouchedInnerSourceCellUpdatedValueByTag.keys.toMutableSet()

                // Collect all initial changed inner cell vertices
                val initialChangedInnerCellVertexByTag: MutableMap<Tag, CellVertex<ElementT>> =
                    initialSourceBagOngoingChange.changedElementByTag.mapValuesTo(mutableMapOf()) { (_, changedInnerCell) ->
                        changedInnerCell.vertex
                    }

                this.changedInnerCellVertexByTag = initialChangedInnerCellVertexByTag

                initialChangedInnerCellVertexByTag.entries.associateTo(initialUpstreamNewInnerCellListenerEntryByTag) { (changedTag, initialChangedInnerCellVertex) ->
                    val listenerHandle = initialChangedInnerCellVertex.registerBoundListener(
                        propagationContext = propagationContext,
                        listener = InnerCellListener(tag = changedTag),
                        mode = mode,
                    )

                    changedTag to InnerCellListenerEntry(
                        cellVertex = initialChangedInnerCellVertex,
                        listenerHandle = listenerHandle,
                    )
                }

                val initialChangedInnerSourceCellNewValueByTag: Map<Tag, ElementT> =
                    initialChangedInnerCellVertexByTag.entries.associate { (changedTag, changedInnerCellVertex) ->
                        changedTag to changedInnerCellVertex.getNewValue(
                            propagationContext = propagationContext,
                        )
                    }

                // Collect all initial removed inner cell vertex tags
                val initialRemovedInnerCellVertexTags: MutableSet<Tag> =
                    initialSourceBagOngoingChange.removedTags.toMutableSet()

                this.removedInnerCellVertexTags = initialRemovedInnerCellVertexTags

                TaggedBagChange(
                    changedElementByTag = initialChangedInnerSourceCellNewValueByTag + initialUntouchedInnerSourceCellUpdatedValueByTag,
                    removedTags = initialRemovedInnerCellVertexTags,
                )
            }
        }
    }

    override fun deactivate() {
        val upstreamSourceListenerHandle =
            this.upstreamSourceListenerHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        this.upstreamSourceListenerHandle = null

        val upstreamNewInnerCellListenerEntryByTag = this.upstreamNewInnerCellListenerEntryByTag
            ?: throw IllegalStateException("Vertex doesn't seem to be active")

        this.upstreamNewInnerCellListenerEntryByTag = null

        // Unregister from source bag vertex
        // FIXME: Untested expression (this might require listener check on the inputs)
        outerSourceBagVertex.unregisterListener(
            handle = upstreamSourceListenerHandle,
        )

        // Unregister from all inner cell vertices
        for ((_, listenerEntry) in upstreamNewInnerCellListenerEntryByTag) {
            val newCellVertex = listenerEntry.cellVertex
            val newListenerHandle = listenerEntry.listenerHandle

            // FIXME: Untested expression (this might require listener check on the inputs)
            newCellVertex.unregisterListener(
                handle = newListenerHandle,
            )
        }

        this.stableInnerSourceCellVertexByTag = null

        this.changedInnerCellVertexByTag = null
        this.removedInnerCellVertexTags = null

        this.updatedUntouchedStableInnerCellVertexTags = null
    }

    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): TaggedBag<ElementT> = when (val stableInnerSourceCellVertexByTag = this.stableInnerSourceCellVertexByTag) {
        // Inactive vertex - compute lazily
        null -> {
            val sourceContent = outerSourceBagVertex.getOldContentView(propagationContext)

            LazyFusedTaggedBag(
                sourceBag = sourceContent,
                propagationContext = propagationContext,
            )
        }

        // Active vertex - use stored entries
        else -> {
            ActiveFusedTaggedBag(
                stableCellVertexByTag = stableInnerSourceCellVertexByTag,
                propagationContext = propagationContext,
            )
        }
    }

    private fun buildChange(
        propagationContext: Transactions.PropagationContext,
    ): TaggedBagChange<ElementT>? {
        val updatedUntouchedStableInnerCellVertexTags = this.updatedUntouchedStableInnerCellVertexTags
            ?: throw IllegalStateException("Vertex doesn't seem to be active")

        val changedInnerCellVertexByTag = this.changedInnerCellVertexByTag
        val removedInnerCellVertexTags = this.removedInnerCellVertexTags

        val untouchedInnerCellUpdatedValueByTag =
            updatedUntouchedStableInnerCellVertexTags.associateWith { updatedStableTag ->
                val stableCellVertex = this.stableInnerSourceCellVertexByTag?.get(updatedStableTag)
                    ?: throw IllegalStateException("Stable inner cell vertex not found for tag: $updatedStableTag")

                val ongoingUpdate = stableCellVertex.ongoingUpdate
                    ?: throw IllegalStateException("Ongoing update not found for 'updated' cell vertex with tag $updatedStableTag")

                ongoingUpdate.updatedValue
            }.toMap()

        val changedInnerCellNewValueByTag = when (changedInnerCellVertexByTag) {
            null -> emptyMap()
            else -> changedInnerCellVertexByTag.entries.associate { (changedTag, changedInnerCellVertex) ->
                changedTag to changedInnerCellVertex.getNewValue(propagationContext = propagationContext)
            }
        }

        val changedElementByTag = changedInnerCellNewValueByTag + untouchedInnerCellUpdatedValueByTag

        return TaggedBagChange.of(
            changedElementByTag = changedElementByTag,
            removedTags = removedInnerCellVertexTags ?: emptySet(),
        )
    }

    override fun commit(
        ongoingChange: TaggedBagChange<ElementT>?,
    ) {
        val stableInnerSourceCellVertexByTag = this.stableInnerSourceCellVertexByTag ?: return

        val changedInnerCellVertexByTag = this.changedInnerCellVertexByTag

        this.changedInnerCellVertexByTag = null

        val removedInnerCellVertexTags = this.removedInnerCellVertexTags

        this.removedInnerCellVertexTags = null

        this.updatedUntouchedStableInnerCellVertexTags = null

        removedInnerCellVertexTags?.forEach { tag ->
            // FIXME: Untested expression
            stableInnerSourceCellVertexByTag.remove(tag)
        }

        changedInnerCellVertexByTag?.forEach { (tag, changedCellVertex) ->
            // FIXME: Untested expression
            stableInnerSourceCellVertexByTag[tag] = changedCellVertex
        }
    }

    /**
     * Lazy implementation of TaggedBag for inactive vertex.
     */
    private inner class LazyFusedTaggedBag(
        private val sourceBag: TaggedBag<Cell<ElementT>>,
        private val propagationContext: Transactions.PropagationContext,
    ) : TaggedBag<ElementT> {
        override val size: Int
            get() = sourceBag.size

        override val elementByTag: Map<Tag, ElementT>
            get() = sourceBag.elementByTag.mapValues { (_, cell) ->
                cell.vertex.getOldValue(propagationContext)
            }

        override fun getByTag(
            tag: Tag,
        ): ElementT? = sourceBag.getByTag(tag)?.vertex?.getOldValue(propagationContext)

        override fun containsTag(tag: Tag): Boolean = sourceBag.containsTag(tag)

        override fun isEmpty(): Boolean = sourceBag.isEmpty()

        override fun iterator(): Iterator<ElementT> = sourceBag.elementByTag.values.map { cell ->
            cell.vertex.getOldValue(propagationContext)
        }.iterator()

        override fun containsAll(
            elements: Collection<ElementT>,
        ): Boolean {
            val values = elementByTag.values.toSet()
            return values.containsAll(elements)
        }

        override fun contains(
            element: ElementT,
        ): Boolean = elementByTag.values.contains(element)
    }

    /**
     * Active implementation of TaggedBag using stored entries.
     */
    private inner class ActiveFusedTaggedBag(
        private val stableCellVertexByTag: Map<Tag, CellVertex<ElementT>>,
        private val propagationContext: Transactions.PropagationContext,
    ) : TaggedBag<ElementT> {
        override val size: Int
            get() = stableCellVertexByTag.size

        override val elementByTag: Map<Tag, ElementT>
            get() = stableCellVertexByTag.mapValues { (_, stableCellVertex) ->
                stableCellVertex.getOldValue(
                    propagationContext = propagationContext,
                )
            }

        override fun getByTag(tag: Tag): ElementT? = stableCellVertexByTag[tag]?.getOldValue(
            propagationContext = propagationContext,
        )

        override fun containsTag(tag: Tag): Boolean = stableCellVertexByTag.containsKey(tag)

        override fun isEmpty(): Boolean = stableCellVertexByTag.isEmpty()

        override fun iterator(): Iterator<ElementT> = stableCellVertexByTag.values.map { stableCellVertex ->
            stableCellVertex.getOldValue(propagationContext)
        }.iterator()

        override fun containsAll(elements: Collection<ElementT>): Boolean {
            val values = elementByTag.values.toSet()
            return values.containsAll(elements)
        }

        override fun contains(element: ElementT): Boolean = elementByTag.values.contains(element)
    }
}
