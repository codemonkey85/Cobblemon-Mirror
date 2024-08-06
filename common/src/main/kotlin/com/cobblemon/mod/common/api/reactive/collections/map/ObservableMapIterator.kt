package com.cobblemon.mod.common.api.reactive.collections.map

import kotlin.collections.Map.Entry

open class ObservableMapIterator<K, V>(
    protected var iterator: Iterator<Entry<K, V>>
) : Iterator<Entry<K, V>> {

    override fun hasNext():Boolean = iterator.hasNext()
    override fun next(): Entry<K, V> = iterator.next()
}
