package com.cobblemon.mod.common.api.reactive.collections.map

import kotlin.collections.Map.Entry

open class MutableObservableMapIterator<K, V>(
    private val observableMap: MutableObservableMap<K, V>,
) : ObservableMapIterator<K, V>(observableMap.entries.toSet().iterator()),
    MutableIterator<Entry<K, V>> {

    private lateinit var currentEntry: Entry<K, V>

    private var removedThisIteration: Boolean? = null
        set (value) { if (field != value) field = value }

    override fun next(): Entry<K, V> {
        currentEntry = super.next()
        removedThisIteration = false
        return currentEntry
    }

    override fun remove() {
        if (removedThisIteration == null) {
            throw IllegalStateException(
                "Observable Map iterator 'remove' was called before 'next' or 'previous'."
            )
        } else if (removedThisIteration == true) {
            throw IllegalStateException(
                "Observable Map iterator 'remove' was called, " +
                        "but 'remove' was already called this iteration."
            )
        }
        observableMap.remove(currentEntry.key)
        removedThisIteration = true
    }
}
