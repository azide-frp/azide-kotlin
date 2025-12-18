package dev.azide.internal.utils.weak_bag

import kotlin.jvm.JvmInline

/**
 * A very simple implementation of a _weak bag_. A bag is a collection that can hold multiple instances of the same
 * value, without keeping any specific internal order, nor guaranteeing any efficient lookup operations.
 */
class MutableBag<ElementT : Any> {
    /**
     * A linked list storing bag's elements. A linked list is the simplest data structure that allows efficient addition
     * and removal of elements given a handle to them. Bags don't benefit from more complex data structures.
     */
    private val _linkedList = RawLinkedList<ElementT>()

    private var _size = 0

    /**
     * The number of elements currently present in the bag.
     */
    val size: Int
        get() = _size

    @JvmInline
    value class Handle<ElementT : Any>(
        val node: RawLinkedList.Node<ElementT>,
    )

    /**
     * Adds the given element to the bag.
     *
     * @return a handle that can be used to remove the element later
     */
    fun add(
        element: ElementT,
    ): Handle<ElementT> {
        val listHandle = _linkedList.prepend(element)

        ++_size

        return Handle(
            node = listHandle,
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
        _linkedList.remove(
            node = handle.node,
        )

        --_size
    }

    /**
     * Executes the given [action] for each element currently present in the list. The order in which the elements
     * are visited is unspecified. Purges any unused internal entries.
     *
     * @param action the action to execute for each element. If it returns `true`, the given element will be removed from
     * the bag and the iteration will continue from another unvisited element.
     */
    fun forEach(
        action: (ElementT) -> Boolean,
    ) {
        // In practice, the iteration order will be from the most recently added to the least recently added element.
        // The user should not rely on this behavior, though.

        _linkedList.forEach(action)
    }
}
