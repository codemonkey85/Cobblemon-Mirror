package com.cobblemon.mod.common.api.reactive.collections

interface MutableObservableCollectionIterator<T, C : Collection<T>, I : Iterator<T>> :
    ObservableCollectionIterator<T, I>,
    MutableIterator<T> {

    override fun remove()
}
