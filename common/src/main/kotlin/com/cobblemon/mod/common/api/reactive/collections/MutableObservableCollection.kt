package com.cobblemon.mod.common.api.reactive.collections

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.collections.subscriptions.CollectionSubscription

interface MutableObservableCollection<T, C : Collection<T>> :
    ObservableCollection<T, C>,
    MutableCollection<T> {

    fun subscribe(
        priority: Priority = Priority.NORMAL,
        anyChangeHandler: (Pair<C, T>) -> Unit,
        additionHandler: ((Pair<C, T>) -> Unit)? = null,
        removalHandler: ((Pair<C, T>) -> Unit)? = null,
    ): CollectionSubscription<T, C>

    override fun add(element: T): Boolean

    fun addIf(element: T, predicate: (C) -> Boolean): Boolean

    override fun remove(element: T): Boolean

    fun removeIf(predicate: (T) -> Boolean): Boolean

    override fun addAll(elements: Collection<T>): Boolean

    override fun retainAll(elements: Collection<T>): Boolean

    override fun removeAll(elements: Collection<T>): Boolean

    override fun iterator(): MutableIterator<T>

    override fun clear()

    fun mutableCopy(): MutableObservableCollection<T, C>
}
