package dev.azide.core.internal.utils.weak_bag

import dev.azide.core.internal.utils.weak_bag.RawLinkedList
import dev.azide.core.internal.utils.weak_bag.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("ClassName")
class RawLinkedList_tests {
    @Test
    fun testPrepend_toEmpty_initial() {
        val linkedList = RawLinkedList<Int>()

        val prependedNode = linkedList.prepend(10)

        assertEquals(
            expected = 10,
            actual = prependedNode.element,
        )

        assertEquals(
            expected = listOf(10),
            actual = linkedList.toList(),
        )
    }

    @Test
    fun testPrepend_toEmpty_afterRemoval() {
        val linkedList = RawLinkedList<Int>()

        val earlierNode1 = linkedList.prepend(-1)
        val earlierNode2 = linkedList.prepend(-2)
        val earlierNode3 =  linkedList.prepend(-3)

        linkedList.remove(earlierNode2)
        linkedList.remove(earlierNode3)
        linkedList.remove(earlierNode1)

        val prependedNode = linkedList.prepend(10)

        assertEquals(
            expected = 10,
            actual = prependedNode.element,
        )

        assertEquals(
            expected = listOf(10),
            actual = linkedList.toList(),
        )
    }

    @Test
    fun testPrepend_toNonEmpty() {
        val linkedList = RawLinkedList<Int>()

        linkedList.prepend(10)

        val prependedNode = linkedList.prepend(20)

        assertEquals(
            expected = 20,
            actual = prependedNode.element,
        )

        assertEquals(
            expected = listOf(20, 10),
            actual = linkedList.toList(),
        )
    }

    @Test
    fun testRemove_onlyElement() {
        val list = RawLinkedList<Int>()

        val singleNode = list.prepend(42)

        list.remove(singleNode)

        assertEquals(
            expected = emptyList(),
            actual = list.toList(),
        )
    }

    @Test
    fun testRemove_first_fromNonEmpty() {
        val linkedList = RawLinkedList<Int>()

        val firstNode = linkedList.prepend(1)

        linkedList.prepend(2)
        linkedList.prepend(3)

        linkedList.remove(firstNode)

        assertEquals(
            expected = listOf(3, 2),
            actual = linkedList.toList(),
        )
    }

    @Test
    fun testRemove_inner() {
        val list = RawLinkedList<Int>()

        list.prepend(1)

        val innerNode = list.prepend(2)

        list.prepend(3)

        list.remove(innerNode)

        assertEquals(
            expected = listOf(3, 1),
            actual = list.toList(),
        )
    }


    @Test
    fun testRemove_last_fromNonEmpty() {
        val linkedList = RawLinkedList<Int>()

        linkedList.prepend(1)
        linkedList.prepend(2)

        val lastNode = linkedList.prepend(3)

        linkedList.remove(lastNode)

        assertEquals(
            expected = listOf(2, 1),
            actual = linkedList.toList(),
        )
    }

    @Test
    fun testRemove_all() {
        val list = RawLinkedList<Int>()

        val node1 = list.prepend(1)
        val node2 = list.prepend(2)
        val node3 = list.prepend(3)

        list.remove(node2)
        list.remove(node1)
        list.remove(node3)

        assertEquals(
            expected = emptyList(),
            actual = list.toList(),
        )
    }

    @Test
    fun testRemove_alreadyRemoved_throws() {
        val list = RawLinkedList<Int>()

        val node = list.prepend(42)

        list.remove(node)

        assertFailsWith<IllegalArgumentException> {
            list.remove(node)
        }
    }
}
