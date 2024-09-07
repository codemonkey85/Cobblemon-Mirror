package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables
import net.minecraft.nbt.NbtCompound


fun <T> Set<T>.saveToNbt(
    elementKey: String = "element",
    elementHandler: (T) -> NbtCompound,
): NbtCompound {
    val nbt = NbtCompound()
    var size = 0
    this.iterator().forEach { nbt.put(elementKey + size++, elementHandler(it)) }
    nbt.putInt("size", size)
    return nbt
}


fun <T> NbtCompound.loadObservableSetOf(
    elementKey: String = "element",
    loadElementHandler: (NbtCompound) -> T,
    elementObservableHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = ObservableSet(this.loadSet(elementKey, loadElementHandler), elementObservableHandler)


fun <T> NbtCompound.loadMutableObservableSetOf(
    elementKey: String = "element",
    loadElementHandler: (NbtCompound) -> T,
    elementObservableHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = MutableObservableSet(this.loadSet(elementKey, loadElementHandler), elementObservableHandler)


private fun <T> NbtCompound.loadSet(
    elementKey: String = "element",
    elementHandler: (NbtCompound) -> T,
): Set<T> {
    val newSet = mutableSetOf<T>()
    if (this.contains("size")) {
        val size = this.getInt("size")
        for (i in 0 until size) {
            newSet.add(elementHandler(this.getCompound(elementKey + i)))
        }
    }
    return newSet
}
