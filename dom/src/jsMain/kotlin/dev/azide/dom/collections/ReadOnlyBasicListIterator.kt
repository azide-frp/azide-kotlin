package dev.azide.dom.collections

class ReadOnlyBasicListIterator<out E>(
    override val list: List<E>,
    index: Int = 0,
) : BasicListIterator<E>(index)
