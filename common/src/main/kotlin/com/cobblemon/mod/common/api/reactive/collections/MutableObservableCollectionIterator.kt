package com.cobblemon.mod.common.api.reactive.collections

interface MutableObservableCollectionIterator<T, C : Collection<T>> :
    ObservableCollectionIterator<T, Iterator<T>>,
    MutableIterator<T> {

    override fun remove()
}
