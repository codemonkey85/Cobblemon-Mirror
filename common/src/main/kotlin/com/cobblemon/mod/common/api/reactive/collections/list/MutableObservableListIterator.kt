package com.cobblemon.mod.common.api.reactive.collections.list

// the name is not quite long enough yet...
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollectionIterator

open class MutableObservableListIterator<T>(
    private val observableList: MutableObservableList<T>,
    index: Int = 0,
) : MutableObservableCollectionIterator<T, Set<T>>,
    ObservableListIterator<T>(observableList.elements.toMutableList().listIterator(index), index),
    MutableListIterator<T> {

    override fun remove() {
        if (index < 0) {
            throw IllegalStateException("Observable List iterator 'remove' was called before 'next' or 'previous'.")
        } else if (removedOrAddedThisIteration) {
            throw IllegalStateException(
                "Observable List iterator 'remove' was called, but 'remove' or 'add' were already called this iteration."
            )
        }
        iterator.remove()
        observableList.removeAt(index--)
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
        iterator.set(element)
        observableList[index] = element
    }

    override fun add(element: T) {
        iterator.add(element)
        when {
            observableList.isEmpty() -> observableList.add(element).run { index = 0 }
            index < observableList.lastIndex -> observableList.add(++index, element)
            else -> observableList.add(element).run { index++ }
        }
        removedOrAddedThisIteration = true
    }
}
