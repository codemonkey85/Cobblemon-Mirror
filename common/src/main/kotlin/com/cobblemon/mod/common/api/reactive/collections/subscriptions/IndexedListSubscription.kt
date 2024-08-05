package com.cobblemon.mod.common.api.reactive.collections.subscriptions

import com.cobblemon.mod.common.api.reactive.ObservableSubscription

data class IndexedListSubscription<T>(
    val addition: ObservableSubscription<Triple<List<T>, T, Int>>? = null,
    val removal: ObservableSubscription<Triple<List<T>, T, Int>>? = null,
    val set: ObservableSubscription<Triple<List<T>, Pair<T, T>, Int>>? = null,
    val swap: ObservableSubscription<Triple<List<T>, Pair<T, T>, Pair<Int, Int>>>? = null,
) {

    fun unsubscribe() {
        addition?.unsubscribe()
        removal?.unsubscribe()
        set?.unsubscribe()
        swap?.unsubscribe()
    }
}
