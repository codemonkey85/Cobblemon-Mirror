package com.cobblemon.mod.common.api.reactive.collections.subscriptions

import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.collections.map.ObservableMap
import com.cobblemon.mod.common.api.reactive.collections.map.MutableObservableMap

/**
 * The immutable [ObservableMap] implements an [anyChange] observable subscription directly.
 *
 * The [MutableObservableMap] implements [anyChange], [addition], & [removal] subscriptions
 * through this class.
 *
 * @property anyChange Emits with any mutation from removal, addition, or a mutation in place by
 * an entry extending the observable interface.
 *
 * @property addition Emits when any mutation results from the addition of an element.
 *
 * @property removal Emits when any mutation results from the removal of an element.
 */
data class MapSubscription<K, T>(
    val anyChange: ObservableSubscription<Pair<Map<K, T>, Pair<K, T>>>,
    val addition: ObservableSubscription<Pair<Map<K, T>, Pair<K, T>>>? = null,
    val removal: ObservableSubscription<Pair<Map<K, T>, Pair<K, T>>>? = null,
) {

    fun unsubscribe() {
        anyChange.unsubscribe()
        addition?.unsubscribe()
        removal?.unsubscribe()
    }
}
