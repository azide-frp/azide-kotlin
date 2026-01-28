package dev.azide.dom.collections

interface BasicMutableList<E> : MutableList<E>, BasicList<E> {
    override fun addAll(elements: Collection<E>): Boolean {
        var added = false

        elements.forEach { element ->
            if (add(element)) {
                added = true
            }
        }

        return added
    }

    override fun addAll(
        index: Int,
        elements: Collection<E>,
    ): Boolean {
        var movingIndex = index

        if (index !in 0..size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for list of size $size")
        }

        elements.forEach {
            add(movingIndex++, it)
        }

        return true
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var removed = false

        elements.forEach { element ->
            if (remove(element)) {
                removed = true
            }
        }

        return removed
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException()
    }

    override fun clear() {
        while (isNotEmpty()) {
            removeAt(size - 1)
        }
    }

    override fun iterator(): MutableIterator<E> = listIterator()

    override fun listIterator(): MutableListIterator<E> = BasicMutableListIterator(
        list = this,
        index = 0,
    )

    override fun listIterator(index: Int): MutableListIterator<E> {
        if (index !in 0..size) {
            throw IndexOutOfBoundsException("Index $index is out of bounds for list of size $size")
        }

        return BasicMutableListIterator(
            list = this,
            index = index,
        )
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        throw UnsupportedOperationException()
    }
}
