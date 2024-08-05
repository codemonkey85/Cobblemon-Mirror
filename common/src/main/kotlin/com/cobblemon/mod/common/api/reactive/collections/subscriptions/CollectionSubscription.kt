package com.cobblemon.mod.common.api.reactive.collections.subscriptions

import com.cobblemon.mod.common.api.reactive.ObservableSubscription

/**
 * Immutable observable collections implement an [anyChange] observable subscription directly.
 *
 * Mutable observable collections implement [anyChange], [addition], & [removal] subscriptions
 * through a [CollectionSubscription].
 *
 * @property anyChange Emits with any mutation of the collection or the collection's observable elements.
 *
 * @property addition Emits when any collection mutation results from the addition of an element.
 *
 * @property removal Emits when any collection mutation results from the removal of an element.
 *
 * @see ListSubscription.set
 * @see ListSubscription.swap
 */
interface CollectionSubscription<T, C : Collection<T>> {

    val anyChange: ObservableSubscription<Pair<C, T>>
    val addition: ObservableSubscription<Pair<C, T>>?
    val removal: ObservableSubscription<Pair<C, T>>?

    fun unsubscribe()
}
