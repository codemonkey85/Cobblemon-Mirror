package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.list.MutableObservableList
import com.cobblemon.mod.common.api.reactive.collections.list.ObservableList

open class TestLists<T>(final override val input: List<T>) :
    TestCollections<T, List<T>, MutableList<T>> {

    override val mutableInput: MutableList<T> = input.toMutableList()
    override val observable: ObservableList<T> = ObservableList(input)
    override val mutableObservable: MutableObservableList<T> = MutableObservableList(input)
    override val emptyObservable = ObservableList<T>(setOf())
    override val emptyMutableObservable = MutableObservableList<T>()

    companion object {
        fun default() = testListsOf("zero", "one", "two", "three")
        fun defaultList() = listOf("zero", "one", "two", "three")
    }
}

fun <T> testListsOf(vararg elements: T) = TestLists(listOf(*elements))
