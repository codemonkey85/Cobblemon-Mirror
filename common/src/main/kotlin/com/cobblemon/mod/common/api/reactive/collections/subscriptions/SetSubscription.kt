package com.cobblemon.mod.common.api.reactive.collections.subscriptions

import com.cobblemon.mod.common.api.reactive.ObservableSubscription

data class SetSubscription<T>(
    override val anyChange: ObservableSubscription<Pair<Set<T>, T>>,
    override val addition: ObservableSubscription<Pair<Set<T>, T>>? = null,
    override val removal: ObservableSubscription<Pair<Set<T>, T>>? = null,
) : CollectionSubscription<T, Set<T>> {

    override fun unsubscribe() {
        this.anyChange.unsubscribe()
        addition?.unsubscribe()
        removal?.unsubscribe()
    }
}
