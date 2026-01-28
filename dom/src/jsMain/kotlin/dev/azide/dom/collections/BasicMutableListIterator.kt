package dev.azide.dom.collections

class BasicMutableListIterator<E>(
    override val list: MutableList<E>,
    index: Int,
) : BasicListIterator<E>(
    index = index,
), MutableListIterator<E> {
    override fun remove() {
        if (index <= 0 || index > list.size) {
            throw IllegalStateException("Cannot remove element at index $index")
        }

        list.removeAt(--index)
    }

    override fun set(element: E) {
        if (index <= 0 || index > list.size) {
            throw IllegalStateException("Cannot set element at index $index")
        }

        list[index - 1] = element
    }

    override fun add(element: E) {
        if (index < 0 || index > list.size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for list of size ${list.size}")
        }

        list.add(index++, element)
    }
}
