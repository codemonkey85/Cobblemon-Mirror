package com.cobblemon.mod.common.api.reactive.collections.subscriptions

import com.cobblemon.mod.common.api.reactive.ObservableSubscription

data class ListSubscription<T>(
    override val anyChange: ObservableSubscription<Pair<List<T>, T>>,
    override val addition: ObservableSubscription<Pair<List<T>, T>>? = null,
    override val removal: ObservableSubscription<Pair<List<T>, T>>? = null,
    val set: ObservableSubscription<Pair<List<T>, Pair<T, T>>>? = null,
    val swap: ObservableSubscription<Pair<List<T>, Pair<T, T>>>? = null,
) : CollectionSubscription<T, List<T>> {

    override fun unsubscribe() {
        this.anyChange.unsubscribe()
        addition?.unsubscribe()
        removal?.unsubscribe()
        set?.unsubscribe()
        swap?.unsubscribe()
    }
}
