package com.cobblemon.mod.common.api.reactive.collections.map

import kotlin.collections.Map.Entry

open class ObservableMapIterator<K, V>(
    protected var iterator: MutableIterator<Entry<K, V>>
) : Iterator<Entry<K, V>> {

    protected var currentEntry: Entry<K, V>? = null
        set (value) { if (field != value) field = value }

    protected var removedThisIteration: Boolean = false
        set (value) { if (field != value) field = value }

    override fun hasNext():Boolean = iterator.hasNext()

    override fun next(): Entry<K, V> = iterator.next().also { updateState(it) }

    private fun updateState(nextEntry: Entry<K, V>) {
        currentEntry = nextEntry
        removedThisIteration = false
    }
}
