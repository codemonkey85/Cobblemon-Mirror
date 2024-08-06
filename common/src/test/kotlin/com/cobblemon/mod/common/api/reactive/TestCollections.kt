package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.ObservableCollection

interface TestCollections<T, C : Collection<T>, M : MutableCollection<T>> {

    val input: C
    val mutableInput: M
    val observable: ObservableCollection<T, C>
    val mutableObservable: MutableObservableCollection<T, C>
    val emptyObservable: ObservableCollection<T, C>
    val emptyMutableObservable: MutableObservableCollection<T, C>
}
