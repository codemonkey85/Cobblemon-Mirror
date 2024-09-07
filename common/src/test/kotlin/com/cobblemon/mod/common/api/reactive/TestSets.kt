package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.set.MutableObservableSet
import com.cobblemon.mod.common.api.reactive.collections.set.ObservableSet


class TestSets<T>(override val input: Set<T>) :
    TestCollections<T, Set<T>, MutableSet<T>> {

    override val mutableInput: MutableSet<T> = input.toMutableSet()
    override val observable: ObservableSet<T> = ObservableSet(input)
    override val mutableObservable: MutableObservableSet<T> = MutableObservableSet(input)
    override val emptyObservable = ObservableSet<T>(setOf())
    override val emptyMutableObservable = MutableObservableSet<T>()

    companion object {
        fun default() = testSetsOf("zero", "one", "two", "three")
        fun defaultSet() = setOf("zero", "one", "two", "three")
    }
}

fun <T> testSetsOf(vararg elements: T) = TestSets(setOf(*elements))
