package com.cobblemon.mod.common.api.reactive.collections.list

open class ObservableListIterator<T>(
    protected var list: ObservableList<T>,
    protected var index: Int = 0,
) : ListIterator<T> {

    override fun hasNext(): Boolean = index < list.size

    override fun hasPrevious(): Boolean = list.isNotEmpty() && index > 0

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException(
                "Observable List iterator does not have next element from index $index."
            )
        }
        return list[index++]
    }

    override fun previous(): T {
        if (!hasPrevious()) {
            throw NoSuchElementException(
                "Observable List iterator does not have a previous element at index ${index -1}."
            )
        }
        return list[--index]
    }

    override fun nextIndex(): Int = index

    override fun previousIndex(): Int =  index -1
}
