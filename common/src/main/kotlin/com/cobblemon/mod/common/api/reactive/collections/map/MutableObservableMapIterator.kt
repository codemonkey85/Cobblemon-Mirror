package com.cobblemon.mod.common.api.reactive.collections.map

import kotlin.collections.Map.Entry

open class MutableObservableMapIterator<K, V>(
    private val observableMap: MutableObservableMap<K, V>,
) : ObservableMapIterator<K, V>(observableMap.toMutableMap().iterator()),
    MutableIterator<Entry<K, V>> {

    override fun remove() {
        if (currentEntry == null) {
            throw IllegalStateException("Observable Map iterator 'remove' was called before 'next' or 'previous'.")
        } else if (removedThisIteration) {
            throw IllegalStateException(
                "Observable Map iterator 'remove' was called, but 'remove' was already called this iteration."
            )
        }
        iterator.remove()
        observableMap.remove(currentEntry!!.key)
        currentEntry = null
        removedThisIteration = true
    }
}
