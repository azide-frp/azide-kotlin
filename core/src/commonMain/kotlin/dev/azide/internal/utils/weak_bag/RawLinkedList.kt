package dev.azide.internal.utils.weak_bag

/**
 * A very simple implementation of a double linked list data structure.
 */
class RawLinkedList<ElementT> {
    @Suppress("PropertyName")
    class Node<ElementT>(
        val element: ElementT,
    ) {
        private var _isValid = true
        internal var _previous: Node<ElementT>? = null
        internal var _next: Node<ElementT>? = null

        val previous: Node<ElementT>?
            get() = _previous

        val next: Node<ElementT>?
            get() = _next

        /**
         * Returns whether this node is still part of the list.
         */
        val isValid: Boolean
            get() = _isValid

        internal fun invalidate() {
            _isValid = false
        }
    }

    /**
     * The head of the list (the first node), or null if the list is empty.
     */
    private var _head: Node<ElementT>? = null

    val head: Node<ElementT>?
        get() = _head

    /**
     * Adds the given [element] to the front of the list.
     *
     * @return the node corresponding to the added element
     */
    fun prepend(
        element: ElementT,
    ): Node<ElementT> {
        val newNode = Node(
            element = element,
        )

        _head?.let { currentHead ->
            // Link with the current head (if present)
            currentHead._previous = newNode
            newNode._next = currentHead
        }

        // Replace the head
        _head = newNode

        return newNode
    }

    /**
     * Removes the given [node] from the list.
     *
     * @throws IllegalArgumentException if the node has already been removed
     * @return the node that followed the removed node, or null if the removed node was the last one
     *
     */
    fun remove(
        node: Node<ElementT>,
    ): Node<ElementT>? {
        require(node.isValid) { "Node has already been removed" }

        val previousNode = node._previous
        val nextNode = node._next

        // Update previous node's next pointer
        if (previousNode != null) {
            previousNode._next = nextNode
        } else {
            // This was the head
            _head = nextNode
        }

        // Update next node's previous pointer
        if (nextNode != null) {
            nextNode._previous = previousNode
        }

        // Clear the node's pointers and mark as invalid
        node._previous = null
        node._next = null

        node.invalidate()

        return nextNode
    }
}

/**
 * Executes the given [action] for each element currently present in the list. The order in which the elements
 * are visited is from head to tail.
 *
 * @param action the action to execute for each element. If it returns `true`, the given element will be removed from
 * the list and the iteration will continue from the next element.
 */
inline fun <ElementT> RawLinkedList<ElementT>.forEach(
    action: (ElementT) -> Boolean,
) {
    var current = head

    while (current != null) {
        val shouldRemove = action(current.element)

        if (shouldRemove) {
            current = remove(current)
        } else {
            current = current.next
        }
    }
}

fun <ElementT> RawLinkedList<ElementT>.toNodeSequence(): Sequence<RawLinkedList.Node<ElementT>> = generateSequence(
    seedFunction = { head },
    nextFunction = { it.next },
)

fun <ElementT> RawLinkedList<ElementT>.toList(): List<ElementT> = toNodeSequence().map { it.element }.toList()
