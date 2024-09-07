package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.reactive.collections.ObservableCollectionIterator

open class ObservableSetIterator<T>(
    protected var iterator: Iterator<T>
) : ObservableCollectionIterator<T, Iterator<T>> {

    override fun hasNext():Boolean = iterator.hasNext()
    override fun next(): T = iterator.next()
}
