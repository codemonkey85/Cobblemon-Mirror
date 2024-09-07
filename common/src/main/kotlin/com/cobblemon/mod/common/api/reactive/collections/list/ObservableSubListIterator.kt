package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.reactive.collections.ObservableCollectionIterator

open class ObservableSubListIterator<T>(
    protected val subList: ObservableSubList<T>,
    protected var index: Int = 0,
) : ObservableCollectionIterator<T, ListIterator<T>>,
    ListIterator<T> {

    override fun hasNext(): Boolean = index < subList.size

    override fun hasPrevious(): Boolean = subList.isNotEmpty() && index > 0

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException(
                "Observable Sub List iterator does not have next element from index $index."
            )
        }
        return subList[index++]
    }

    override fun previous(): T {
        if (!hasPrevious()) {
            throw NoSuchElementException(
                "Observable Sub List iterator does not have previous element from index ${index - 1}."
            )
        }
        return subList[--index]
    }

    override fun nextIndex(): Int = index

    override fun previousIndex(): Int = index - 1
}
