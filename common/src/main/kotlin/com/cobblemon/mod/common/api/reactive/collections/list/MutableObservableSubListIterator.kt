package com.cobblemon.mod.common.api.reactive.collections.list

// the name is not quite long enough yet...
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollectionIterator

open class MutableObservableSubListIterator<T>(
    private val subList: MutableObservableSubList<T>,
    private var index: Int = 0,
) : MutableObservableCollectionIterator<T, Set<T>>,
    MutableListIterator<T> {

    private var removedOrAddedThisIteration: Boolean = false
        set (value) { if (field != value) field = value }

    init {
        index--
    }

    override fun hasNext(): Boolean = index + 1 < subList.size

    override fun hasPrevious(): Boolean = index - 1 > 0

    override fun next(): T {
        if (!hasNext()) {
            throw NoSuchElementException(
                "Observable Sub List iterator does not have next element from index $index."
            )
        }
        return subList[++index].also { removedOrAddedThisIteration = false }
    }

    override fun previous(): T {
        if (!hasPrevious()) {
            throw NoSuchElementException(
                "Observable Sub List iterator does not have previous element from index $index."
            )
        }
        return subList[--index].also { removedOrAddedThisIteration = false }
    }

    override fun nextIndex(): Int = index + 1

    override fun previousIndex(): Int = index - 1

    override fun remove() {
        if (index < 0) {
            throw IllegalStateException("Observable List iterator 'remove' was called before 'next' or 'previous'.")
        } else if (removedOrAddedThisIteration) {
            throw IllegalStateException(
                "Observable List iterator 'remove' was called, but 'remove' or 'add' were already called this iteration."
            )
        }
        subList.removeAt(index--)
        removedOrAddedThisIteration = true
    }

    override fun set(element: T) {
        if (index < 0) {
            throw IllegalStateException("Observable List iterator 'set' was called before 'next' or 'previous'.")
        } else if (removedOrAddedThisIteration) {
            throw IllegalStateException(
                "Observable List iterator 'set' was called, but 'remove' or 'add' were already called this iteration."
            )
        }
        subList[index] = element
    }

    override fun add(element: T) {
        when {
            subList.isEmpty() -> {
                subList.add(element)
                index = if (subList.isEmpty()) -1 else 0
            }
            index < subList.lastIndex -> subList.add(++index, element)
            else -> subList.add(element).run { index++ }
        }
        removedOrAddedThisIteration = true
    }
}
