package com.cobblemon.mod.common.api.reactive.collections.map

import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.collections.tryGetObservable
import kotlin.collections.Map.Entry


fun <K, V> Entry<K, V>.pair() = this.key to this.value


fun <K, V> Collection<Entry<K, V>>.pairs(): Set<Pair<K, V>> {
    mutableSetOf<Pair<K, V>>().let { set ->
        this.forEach { set.add(it.key to it.value) }
        return set
    }
}


fun <K, V> Map<K, V>.pairs(): Set<Pair<K, V>> {
    mutableSetOf<Pair<K, V>>().let { set ->
        this.forEach { set.add(it.key to it.value) }
        return set
    }
}


fun <K, V> Collection<Entry<K, V>>.toMap(): Map<K, V> = this.toMutableMap()


fun <K, V> Collection<Entry<K, V>>.toMutableMap(): MutableMap<K, V> {
    return mutableMapOf<K, V>().putAll(this)
}


fun <K, V> MutableMap<K, V>.putAll(entries: Collection<Entry<K, V>>): MutableMap<K, V> {
    return entries.forEach { this[it.key] = it.value }.let { this }
}


fun <K, V> defaultEntryHandler() = { k: K, v: V -> k.getEntryObservables(v) }


fun <K, V> K.getEntryObservables(value: V): Set<Observable<*>> {
    val observables = mutableSetOf<Observable<*>>()
    this.tryGetObservable()?.let { observables.add(it) }
    value.tryGetObservable()?.let { observables.add(it) }
    return observables
}


fun <K, V> Map<K, V>.toObservableMap(
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(this, entryHandler)

fun <K, V> Map<K, V>.toMutableObservableMap(
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(this, entryHandler)

fun <K, V> Collection<Pair<K, V>>.toObservableMap(
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(this.toMap(), entryHandler)

fun <K, V> Collection<Pair<K, V>>.toMutableObservableMap(
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(this.toMap(), entryHandler)

fun <K, V> observableMapOf(
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(mapOf(), entryHandler)

fun <K, V> mutableObservableMapOf(
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(mapOf(), entryHandler)

fun <K, V> observableMapOf(
    map: Map<K, V> = emptyMap(),
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(map, entryHandler)

fun <K, V> observableMapOf(
    vararg pairs: Pair<K, V>,
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(pairs.toMap(), entryHandler)

fun <K, V> mutableObservableMapOf(
    vararg pairs: Pair<K, V>,
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(pairs.toMap(), entryHandler)

fun <K, V> mutableObservableMapOf(
    map: Map<K, V> = emptyMap(),
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(map, entryHandler)

fun <K, V> observableMapOf(
    vararg entries: Entry<K, V>,
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(entries.toSet().toMap(), entryHandler)

fun <K, V> mutableObservableMapOf(
    vararg entries: Entry<K, V>,
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(entries.toSet().toMap(), entryHandler)

fun <K, V> observableMapOf(
    pairs: Collection<Pair<K, V>> = emptySet(),
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = ObservableMap(pairs.toMap(), entryHandler)

fun <K, V> mutableObservableMapOf(
    pairs: Collection<Pair<K, V>> = emptySet(),
    entryHandler: (K, V) -> Set<Observable<*>> = defaultEntryHandler(),
) = MutableObservableMap(pairs.toMap(), entryHandler)
