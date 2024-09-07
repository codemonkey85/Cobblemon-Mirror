package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables
import net.minecraft.nbt.NbtCompound


fun <T> List<T>.saveToNbt(
    elementKey: String = "element",
    elementHandler: (T) -> NbtCompound,
): NbtCompound {
    val nbt = NbtCompound()
    var size = 0
    this.iterator().forEach { nbt.put(elementKey + size++, elementHandler(it)) }
    nbt.putInt("size", size)
    return nbt
}


fun <T> NbtCompound.loadObservableListOf(
    elementKey: String = "element",
    loadElementHandler: (NbtCompound) -> T,
    elementObservableHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = ObservableList(this.loadList(elementKey, loadElementHandler), elementObservableHandler)


fun <T> NbtCompound.loadMutableObservableListOf(
    elementKey: String = "element",
    loadElementHandler: (NbtCompound) -> T,
    elementObservableHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = MutableObservableList(this.loadList(elementKey, loadElementHandler), elementObservableHandler)


private fun <T> NbtCompound.loadList(
    elementKey: String = "element",
    elementHandler: (NbtCompound) -> T
): List<T> {
    val newList = mutableListOf<T>()
    if (this.contains("size")) {
        val size = this.getInt("size")
        for (i in 0 until size) {
            newList.add(elementHandler(this.getCompound(elementKey + i)))
        }
    }
    return newList
}
