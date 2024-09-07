package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.collections.ObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables

open class ObservableSet<T>(
    set: Collection<T>,
    protected val elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) : ObservableCollection<T, Set<T>>,
    Set<T>,
    Observable<Pair<Set<T>, T>> {

    protected val set: MutableSet<T> = set.toMutableSet()
    private val subscriptions = PrioritizedList<ObservableSubscription<Pair<Set<T>, T>>>()
    private val subscriptionMap: MutableMap<Observable<*>, ObservableSubscription<*>> = mutableMapOf()

    // Keep as 'this.set' & not 'set,' b/c compiler doesn't like it & cries a little.
    override val elements: Set<T> get() = this.set
    override val size: Int get() = set.size

    init {
        set.forEach { register(it) }
    }

    override fun subscribe(
        priority: Priority,
        handler: (Pair<Set<T>, T>) -> Unit,
    ): ObservableSubscription<Pair<Set<T>, T>> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        return subscription
    }

    fun subscribeAndHandle(
        priority: Priority,
        handler: (Pair<Set<T>, T>) -> Unit
    ): ObservableSubscription<Pair<Set<T>, T>> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        set.forEach { handler(this to it) }
        return subscription
    }

    /*
    TODO: fix issue where passing subscription to addition or removal does not unsubscribe
        Does this issue really matter?
        Calling unsubscribe from the subscription directly works as intended
     */
    override fun unsubscribe(subscription: ObservableSubscription<Pair<Set<T>, T>>) {
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
        subscriptions.forEach { it.handle(set to element) }
        return true
    }

    override fun isEmpty() = set.isEmpty()

    override fun isNotEmpty() = set.isNotEmpty()

    override fun contains(element: T) = set.contains(element)

    override fun containsAll(elements: Collection<T>) = set.containsAll(elements)

    override operator fun iterator(): Iterator<T> = set.iterator()

    override fun copy() = ObservableSet(set, elementHandler)

    fun mutableCopy() = MutableObservableSet(set, elementHandler)
}
