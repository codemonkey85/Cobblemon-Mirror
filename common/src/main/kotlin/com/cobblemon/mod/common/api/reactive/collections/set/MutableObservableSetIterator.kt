package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollectionIterator

open class MutableObservableSetIterator<T>(
    private val observableSet: MutableObservableSet<T>,
) : MutableObservableCollectionIterator<T, Set<T>>,
    ObservableSetIterator<T>(observableSet.elements.toMutableSet().iterator()),
    MutableIterator<T> {

    override fun remove() {
        if (currentElement == null) {
            throw IllegalStateException("Observable Set iterator 'remove' was called before 'next' or 'previous'.")
        } else if (removedThisIteration) {
            throw IllegalStateException(
                "Observable Set iterator 'remove' was called, but 'remove' was already called this iteration."
            )
        }
        iterator.remove()
        observableSet.remove(currentElement)
        currentElement = null
        removedThisIteration = true
    }
}
