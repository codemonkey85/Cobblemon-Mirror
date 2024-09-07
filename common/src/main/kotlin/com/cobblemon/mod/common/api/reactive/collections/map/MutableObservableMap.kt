package com.cobblemon.mod.common.api.reactive.collections.map

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.reactive.collections.subscriptions.MapSubscription
import kotlin.collections.Map.Entry

class MutableObservableMap<K, V>(
    map: Map<K, V> = mapOf(),
    entryHandler: (K, V) -> Set<Observable<*>> = { k, v -> k.getEntryObservables(v) },
) : ObservableMap<K, V>(map = map, entryHandler = entryHandler),
    MutableMap<K, V> {

    private val additionObservable = SimpleObservable<Pair<Map<K, V>, Pair<K, V>>>()
    private val removalObservable = SimpleObservable<Pair<Map<K, V>, Pair<K, V>>>()

    fun subscribe(
        priority: Priority  = Priority.NORMAL,
        anyChangeHandler: (Pair<Map<K, V>, Pair<K, V>>) -> Unit,
        additionHandler: ((Pair<Map<K, V>, Pair<K, V>>) -> Unit)? = null,
        removalHandler: ((Pair<Map<K, V>, Pair<K, V>>) -> Unit)? = null,
    ): MapSubscription<K, V> {
        return MapSubscription(
            anyChange = this.subscribe(priority, anyChangeHandler),
            addition = additionHandler?.let { additionObservable.subscribe(priority, it) },
            removal = removalHandler?.let { removalObservable.subscribe(priority, it) },
        )
    }

    fun subscribeAndHandle(
        priority: Priority  = Priority.NORMAL,
        anyChangeHandler: (Pair<Map<K, V>, Pair<K, V>>) -> Unit,
        additionHandler: ((Pair<Map<K, V>, Pair<K, V>>) -> Unit)? = null,
        removalHandler: ((Pair<Map<K, V>, Pair<K, V>>) -> Unit)? = null,
    ): MapSubscription<K, V> {
        entries.forEach { anyChangeHandler(this to it.pair()) }
        return subscribe(priority, anyChangeHandler, additionHandler, removalHandler)
    }

    /**
     * @return `true` after handling [register] & all subscriptions to [additionObservable],
     * & [ObservableMap.emitAnyChange].
     */
    private fun emitAddition(key: K, value: V): Boolean {
        register(key, value)
        additionObservable.emit(map to (key to value))
        return emitAnyChange(key, value)
    }

    /**
     * @return `true` after handling [unregister] & all subscriptions to [removalObservable],
     * & [ObservableMap.emitAnyChange].
     */
    private fun emitRemoval(key: K, value: V): Boolean {
        unregister(key, value)
        removalObservable.emit(map to (key to value))
        return emitAnyChange(key, value)
    }

    override operator fun iterator(): MutableObservableMapIterator<K, V> {
        return MutableObservableMapIterator(this)
    }

    operator fun set(key: K, value: V): V? = put(key, value)

    fun put(pair: Pair<K, V>): V? = put(pair.first, pair.second)

    fun put(entry: Entry<K, V>): V? = put(entry.key, entry.value)

    override fun put(key: K, value: V): V? {
        val previous = map.put(key, value)
        emitAddition(key, value)
        if (previous != null) {
            emitRemoval(key, previous)
        }
        return previous
    }

    fun remove(key: K, value: V): Boolean {
        return if (map[key] == value) (remove(key) != null) else false
    }

    override fun remove(key: K): V? {
        val value = map.remove(key) ?: return null
        emitRemoval(key, value)
        return value
    }

    override fun putAll(from: Map<out K, V>) = from.forEach { put(it.key, it.value) }

    fun getOrDefault(key: K, defaultValue: V): V = map[key] ?: defaultValue

    fun getOrPut(key: K, newValue: V) = map[key] ?: newValue.also { put(key, it) }

    override fun clear() {
        if (map.isNotEmpty()) {
            keys.toSet().forEach { remove(it) }
        }
    }
}
