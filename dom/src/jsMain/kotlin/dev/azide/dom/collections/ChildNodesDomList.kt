package dev.azide.dom.collections

import org.w3c.dom.ItemArrayLike
import org.w3c.dom.Node

class ChildNodesDomList(
    private val node: Node,
) : MutableDomList<Node>, ItemArrayLikeDomList<Node> {
    override val firstElement: Node?
        get() = node.firstChild

    override fun isNotEmpty(): Boolean = node.hasChildNodes()

    override fun isEmpty(): Boolean = !node.hasChildNodes()

    override fun remove(element: Node): Boolean = when {
        node.contains(element) -> {
            node.removeChild(element)

            true
        }

        else -> false
    }

    override fun set(
        index: Int,
        element: Node,
    ): Node {
        val oldNode =
            getOrNull(index) ?: throw IndexOutOfBoundsException("Index $index is out of bounds for list of size $size")

        if (node.contains(element) && oldNode != element) {
            throw IllegalStateException("Cannot insert a node that is already a child of this node")
        }

        node.replaceChild(
            oldNode,
            element,
        )

        return oldNode
    }

    override fun add(
        index: Int,
        element: Node,
    ) {
        val nextNode = getOrNull(index)

        if (nextNode == null && index != size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for list of size $size")
        }

        if (node.contains(element)) {
            throw IllegalStateException("Cannot insert a node that is already a child of this node")
        }

        node.insertBefore(
            element,
            nextNode,
        )
    }

    override fun removeAt(index: Int): Node {
        val oldNode =
            getOrNull(index) ?: throw IndexOutOfBoundsException("Index $index is out of bounds for list of size $size")

        node.removeChild(oldNode)

        return oldNode
    }

    override fun add(element: Node): Boolean {
        if (node.contains(element)) {
            throw IllegalStateException("Cannot insert a node that is already a child of this node")
        }

        node.appendChild(element)

        return true
    }

    override val itemArrayLike: ItemArrayLike<Node>
        get() = node.childNodes
}

val Node.childNodesList: MutableList<Node>
    get() = ChildNodesDomList(this)
