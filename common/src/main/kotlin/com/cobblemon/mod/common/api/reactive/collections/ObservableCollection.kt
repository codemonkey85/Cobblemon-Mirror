package com.cobblemon.mod.common.api.reactive.collections

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.ObservableSubscription

interface ObservableCollection<T, C : Collection<T>> :
    Collection<T>,
    Observable<Pair<C, T>> {

    val elements: C
    override val size: Int

    override fun subscribe(
        priority: Priority,
        handler: (Pair<C, T>) -> Unit,
    ): ObservableSubscription<Pair<C, T>>

    override fun unsubscribe(subscription: ObservableSubscription<Pair<C, T>>)

    override fun isEmpty(): Boolean

    fun isNotEmpty(): Boolean

    override fun contains(element: T): Boolean

    override fun containsAll(elements: Collection<T>): Boolean

    override operator fun iterator(): Iterator<T>

    fun copy(): ObservableCollection<T, C>
}
