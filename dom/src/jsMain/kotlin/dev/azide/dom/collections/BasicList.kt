package dev.azide.dom.collections

interface BasicList<out E> : List<E> {
    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<E> = listIterator()

    override fun contains(
        element: @UnsafeVariance E,
    ): Boolean {
        for (i in 0 until size) {
            if (get(i) == element) {
                return true
            }
        }

        return false
    }

    override fun containsAll(
        elements: Collection<@UnsafeVariance E>,
    ): Boolean = elements.all { contains(it) }

    override fun indexOf(
        element: @UnsafeVariance E,
    ): Int {
        for (i in 0 until size) {
            if (get(i) == element) {
                return i
            }
        }

        return -1
    }

    override fun lastIndexOf(
        element: @UnsafeVariance E,
    ): Int {
        for (i in size - 1 downTo 0) {
            if (get(i) == element) {
                return i
            }
        }

        return -1
    }

    override fun listIterator(): ListIterator<E> = ReadOnlyBasicListIterator(
        list = this,
        index = 0,
    )

    override fun listIterator(
        index: Int,
    ): ListIterator<E> = ReadOnlyBasicListIterator(
        list = this,
        index = index,
    )

    override fun subList(
        fromIndex: Int,
        toIndex: Int,
    ): List<E> {
        throw UnsupportedOperationException()
    }
}
