package dev.azide.dom.collections

interface DomList<out E: Any> : BasicList<E> {
    override val size: Int

    override fun isEmpty(): Boolean = size == 0

    fun isNotEmpty(): Boolean = size > 0

    override fun get(index: Int): E = getOrNull(index = index) ?: throw IndexOutOfBoundsException(
        "Index $index is out of bounds for size $size",
    )

    fun getOrNull(index: Int): E?

    val firstElement: E?
        get() = getOrNull(0)
}
