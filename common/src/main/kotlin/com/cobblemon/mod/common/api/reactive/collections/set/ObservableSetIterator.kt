package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.reactive.collections.ObservableCollectionIterator

open class ObservableSetIterator<T>(
    protected var iterator: MutableIterator<T>
) : ObservableCollectionIterator<T, Iterator<T>> {

    protected var currentElement: T? = null
        set (value) { if (field != value) field = value }

    protected var removedThisIteration: Boolean = false
        set (value) { if (field != value) field = value }

    override fun hasNext():Boolean = iterator.hasNext()

    override fun next(): T = iterator.next().also { updateState(it) }

    private fun updateState(nextElement: T) {
        currentElement = nextElement
        removedThisIteration = false
    }
}
