package com.cobblemon.mod.common.api.reactive.collections

interface ObservableCollectionIterator<T, I : Iterator<T>> : Iterator<T> {

    override fun hasNext(): Boolean
    override fun next(): T
}
