package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollectionIterator

open class MutableObservableSetIterator<T>(
    private val observableSet: MutableObservableSet<T>,
) : MutableObservableCollectionIterator<T, Set<T>, Iterator<T>>,
    ObservableSetIterator<T>(observableSet.elements.toSet().iterator()),
    MutableIterator<T> {

    private var currentElement: T? = null
        set (value) { if (field != value) field = value }

    private var removedThisIteration: Boolean = false
        set (value) { if (field != value) field = value }

    override fun next(): T {
        currentElement = iterator.next()
        removedThisIteration = false
        return currentElement!!
    }

    override fun remove() {
        if (currentElement == null) {
            throw IllegalStateException("Observable Set iterator 'remove' was called before 'next' or 'previous'.")
        } else if (removedThisIteration) {
            throw IllegalStateException("Observable Set iterator 'remove' was called, but 'remove' was already called this iteration.")
        }
        observableSet.remove(currentElement)
        currentElement = null
        removedThisIteration = true
    }
}
