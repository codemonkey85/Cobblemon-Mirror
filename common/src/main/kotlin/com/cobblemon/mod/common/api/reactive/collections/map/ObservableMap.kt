package com.cobblemon.mod.common.api.reactive.collections.map

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import kotlin.Comparator
import kotlin.collections.Map.Entry

open class ObservableMap<K, V>(
    map: Map<K, V>,
    protected val entryHandler: (K, V) -> Set<Observable<*>> =
        { k: K, v: V -> k.getEntryObservables(v) },
) : Map<K, V>,
    Observable<Pair<Map<K, V>, Pair<K, V>>> {

    protected val map: MutableMap<K, V> = map.toMutableMap()
    private val subscriptions = PrioritizedList<ObservableSubscription<Pair<Map<K, V>, Pair<K, V>>>>()
    private val subscriptionMap: MutableMap<Observable<*>, ObservableSubscription<*>> = mutableMapOf()

    override val size get() = map.size
    override val keys get() = map.keys
    override val values get() = map.values
    override val entries get() = map.entries

    init {
        this.map.forEach { (key, value) ->
            entryHandler(key, value).forEach{ observable ->
                subscriptionMap[observable] = observable.subscribe {
                    emitAnyChange(key, value)
                }
            }
        }
    }

    override fun subscribe(
        priority: Priority,
        handler: (Pair<Map<K, V>, Pair<K, V>>) -> Unit
    ): ObservableSubscription<Pair<Map<K, V>, Pair<K, V>>> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        return subscription
    }

    fun subscribeAndHandle(
        priority: Priority,
        handler: (Pair<Map<K, V>, Pair<K, V>>) -> Unit
    ): ObservableSubscription<Pair<Map<K, V>, Pair<K, V>>> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        map.forEach { handler(this to (it.pair())) }
        return subscription
    }

    override fun unsubscribe(subscription: ObservableSubscription<Pair<Map<K, V>, Pair<K, V>>>) {
        subscriptions.remove(subscription)
    }

    protected open fun register(key: K, value: V) {
        if (contains(key, value)) {
            entryHandler(key, value).forEach{ observable ->
                subscriptionMap[observable] = observable.subscribe { emitAnyChange(key, value) }
            }
        }
    }

    protected fun unregister(key: K, value: V) {
        entryHandler(key, value).forEach{ subscriptionMap.remove(it)?.unsubscribe() }
    }

    protected fun emitAnyChange(key: K, value: V): Boolean {
        subscriptions.forEach { it.handle(map to (key to value)) }
        return true
    }

    override fun isEmpty() = map.isEmpty()

    fun isNotEmpty() = map.isNotEmpty()

    override fun containsKey(key: K) = map.containsKey(key)

    override fun containsValue(value: V) = map.containsValue(value)

    fun contains(pair: Pair<K, V>): Boolean = contains(pair.first, pair.second)

    fun contains(entry: Entry<K, V>): Boolean = contains(entry.key, entry.value)

    fun contains(key: K, value: V) = map[key] == value

    fun contains(predicate: (Entry<K, V>) -> Boolean) = map.any(predicate)

    fun containsAll(other: Collection<Entry<K, V>>): Boolean {
        return map.all { entry -> other.any { it.key == entry.key && it.value == entry.value } }
    }

    fun containsAll(other: Map<K, V>): Boolean {
        return map.all { entry -> other.any { it.key == entry.key && it.value == entry.value } }
    }

    open operator fun iterator(): Iterator<Entry<K, V>> = map.iterator()

    override operator fun get(key: K) = map[key]

    fun toSortedMap(comparator: Comparator<in K>) = map.toMap().toSortedMap(comparator)

    fun firstKeyOrNull(predicate: (Entry<K, V>) -> Boolean = { true }): K? {
        map.forEach { if (predicate(it)) return it.key }
        return null
    }

    fun firstValueOrNull(predicate: (Entry<K, V>) -> Boolean = { true }): V? {
        map.forEach { if (predicate(it)) return it.value }
        return null
    }

    fun firstEntryOrNull(predicate: (Entry<K, V>) -> Boolean = { true }): Entry<K, V>? {
        map.forEach { if (predicate(it)) return it }
        return null
    }

    fun copy() = ObservableMap(this.map, entryHandler)

    fun mutableCopy() = MutableObservableMap(this.map, entryHandler)
}
