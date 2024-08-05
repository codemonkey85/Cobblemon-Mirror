package com.cobblemon.mod.common.api.reactive.collections.map

import com.cobblemon.mod.common.api.reactive.Observable
import net.minecraft.nbt.NbtCompound
import kotlin.collections.Map.Entry


fun <K, V> Map<K, V>.saveToNbt(
    entryKey: String = "entry",
    entryHandler: (Entry<K, V>) -> NbtCompound,
): NbtCompound {
    val nbt = NbtCompound()
    var size = 0
    this.iterator().forEach { nbt.put(entryKey + size++, entryHandler(it)) }
    nbt.putInt("size", size)
    return nbt
}


fun  <K, V> NbtCompound.loadObservableMapOf(
    entryKey: String = "entry",
    loadEntryHandler: (NbtCompound) -> Pair<K, V>,
    entryObservableHandler: (K, V) -> Set<Observable<*>> = { k, v -> k.getEntryObservables(v) },
) = ObservableMap(this.loadMap(entryKey, loadEntryHandler), entryObservableHandler)


fun  <K, V> NbtCompound.loadMutableObservableMapOf(
    entryKey: String = "entry",
    loadEntryHandler: (NbtCompound) -> Pair<K, V>,
    entryObservableHandler: (K, V) -> Set<Observable<*>> = { k, v -> k.getEntryObservables(v) },
) = MutableObservableMap(this.loadMap(entryKey, loadEntryHandler), entryObservableHandler)


private fun <K, V> NbtCompound.loadMap(
    entryKey: String = "entry",
    entryHandler: (NbtCompound) -> Pair<K, V>,
): MutableMap<K, V> {
    val newMap: MutableMap<K, V> = mutableMapOf()
    if (this.contains("size")) {
        val size = this.getInt("size")
        for (i in 0 until size) {
            val (key, value) = entryHandler(this.getCompound(entryKey + i))
            newMap[key] = value
        }
    }
    return newMap
}
