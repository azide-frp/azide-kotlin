package dev.azide.core.internal.utils.weak_bag

import dev.kmpx.platform.PlatformWeakReference
import kotlin.jvm.JvmInline

/**
 * A very simple implementation of a _weak bag_. A bag is a collection that can hold multiple instances of the same
 * value, without keeping any specific internal order, nor guaranteeing any efficient lookup operations. A weak bag is a
 * bag that holds its element values weakly, i.e., allowing them to be garbage collected when there are no strong
 * references to them left.
 */
class MutableWeakBag<ElementT : Any> {
    /**
     * A linked list of weak references. A linked list is the simplest data structure that allows efficient addition
     * and removal of elements given a handle to them. Bags don't benefit from more complex data structures (trees,
     * hash tables, etc.) since they don't provide efficient lookup operations.
     */
    private val weakRefList = RawLinkedList<PlatformWeakReference<ElementT>>()

    @JvmInline
    value class Handle<ElementT : Any>(
        val node: RawLinkedList.Node<PlatformWeakReference<ElementT>>,
    )

    /**
     * Adds the given element to the bag.
     *
     * @return a handle that can be used to remove the element later
     */
    fun add(
        element: ElementT,
    ): Handle<ElementT> {
        val appendedNode = weakRefList.prepend(
            PlatformWeakReference(element),
        )

        return Handle(
            node = appendedNode,
        )
    }

    /**
     * Removes the element corresponding to the given [handle] from the bag.
     *
     * @throws IllegalArgumentException if the corresponding element has already been removed
     */
    fun remove(
        handle: Handle<ElementT>,
    ) {
        weakRefList.remove(
            node = handle.node,
        )
    }

    /**
     * Executes the given [action] for each element currently present in the bag. The order in which the elements
     * are visited is unspecified. Purges any unused internal entries.
     */
    fun forEach(
        action: (ElementT) -> Unit,
    ) {
        // In practice, the iteration order will be from the most recently added to the least recently added element.
        // The user should not rely on this behavior, though.

        weakRefList.forEach { weakReference ->
            when (val element = weakReference.get()) {
                null -> { // The element object was already collected
                    true // Remove the weak reference element
                }

                else -> { // The element object is still alive
                    action(element)

                    false // Keep the weak reference element
                }
            }
        }
    }
}
