package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.collections.ObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables

open class ObservableList<T>(
    list: Collection<T>,
    protected val elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) : ObservableCollection<T, List<T>>,
    List<T>,
    Observable<Pair<List<T>, T>> {

    protected val list: MutableList<T> = list.toMutableList()
    private val subscriptions = PrioritizedList<ObservableSubscription<Pair<List<T>, T>>>()
    private val subscriptionMap: MutableMap<Observable<*>, ObservableSubscription<*>> = mutableMapOf()

    override val elements: List<T> get() = list
    override val size: Int get() = list.size
    val lastIndex: Int get() = list.lastIndex

    init {
        list.forEach { register(it) }
    }

    override fun subscribe(
        priority: Priority,
        handler: (Pair<List<T>, T>) -> Unit,
    ): ObservableSubscription<Pair<List<T>, T>> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        return subscription
    }

    fun subscribeAndHandle(
        priority: Priority,
        handler: (Pair<List<T>, T>) -> Unit
    ): ObservableSubscription<Pair<List<T>, T>> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        list.forEach { handler(this to it) }
        return subscription
    }

    override fun unsubscribe(subscription: ObservableSubscription<Pair<List<T>, T>>) {
        subscriptions.remove(subscription)
    }

    protected fun register(element: T) {
        elementHandler(element).forEach{ observable ->
            subscriptionMap[observable] = observable.subscribe { emitAnyChange(element) }
        }
    }

    protected fun unregister(element: T) {
        elementHandler(element).forEach{ subscriptionMap.remove(it)?.unsubscribe() }
    }

    /** @return true after handling [subscriptions]. */
    protected fun emitAnyChange(element: T): Boolean {
        subscriptions.forEach { it.handle(list to element) }
        return true
    }

    override fun isEmpty() = list.isEmpty()

    override fun isNotEmpty() = list.isNotEmpty()

    override fun contains(element: T) = list.contains(element)

    override fun containsAll(elements: Collection<T>) = list.containsAll(elements)

    override operator fun get(index: Int): T = list[index]

    override fun indexOf(element: T) = list.indexOf(element)

    override operator fun iterator(): Iterator<T> = list.iterator()

    override fun lastIndexOf(element: T) = list.lastIndexOf(element)

    override fun listIterator(): ListIterator<T> = list.listIterator()

    override fun listIterator(index: Int): ListIterator<T> = list.listIterator(index = index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> = list.subList(fromIndex, toIndex)

    override fun copy() = ObservableList(this.list, elementHandler)

    open fun mutableCopy() = MutableObservableList(this.list, elementHandler)
}
