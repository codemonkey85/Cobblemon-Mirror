package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.collections.ObservableCollection

open class ObservableSubList<T>(
    private val parentList: ObservableList<T>,
    private val fromIndex: Int,
    private val toIndex: Int,
    private val elementHandler: (T) -> Set<Observable<*>>,
) : ObservableCollection<T, List<T>>,
    List<T>,
    Observable<Pair<List<T>, T>> {

    private val subscriptions = PrioritizedList<ObservableSubscription<Pair<List<T>, T>>>()
    private val subscriptionMap: MutableMap<Observable<*>, ObservableSubscription<*>> = mutableMapOf()

    override val elements: List<T> = parentList.elements.subList(fromIndex, toIndex)
    override val size: Int = toIndex - fromIndex

    init {
        for (i in fromIndex until toIndex) {
            register(parentList[i])
        }
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
        elements.forEach { handler(this to it) }
        return subscription
    }

    override fun unsubscribe(subscription: ObservableSubscription<Pair<List<T>, T>>) {
        subscriptions.remove(subscription)
    }

    protected fun register(element: T) {
        elementHandler(element).forEach { observable ->
            subscriptionMap[observable] = observable.subscribe { emitAnyChange(element) }
        }
    }

    protected fun unregister(element: T) {
        elementHandler(element).forEach { observable ->
            subscriptionMap.remove(observable)?.unsubscribe()
        }
    }

    protected fun emitAnyChange(element: T): Boolean {
        subscriptions.forEach { it.handle(elements to element) }
        return true
    }

    protected fun Int.isInParentRelativeWindow(): Boolean = this in fromIndex until toIndex

    override fun isEmpty() = parentList.lastIndex < fromIndex

    override fun isNotEmpty() = parentList.size > fromIndex

    override fun contains(element: T) = elements.contains(element)

    override fun containsAll(elements: Collection<T>) = this.elements.containsAll(elements)

    /** @throws IndexOutOfBoundsException if sublist does not contain [subListIndex]. */
    protected fun parentIndex(subListIndex: Int) = throwIfOutOfBounds(subListIndex) + fromIndex
    /**
     * @throws IndexOutOfBoundsException [index] is above [currentLastIndex] or below [fromIndex]
     * @return [index]
     */
    private fun throwIfOutOfBounds(index: Int): Int {
        return if (index in 0 until size) {
            index
        } else {
            throw IndexOutOfBoundsException("Index $index is outside the sub list window.")
        }
    }

    override operator fun get(index: Int): T {
        return parentList[parentIndex(index)]
    }

    override fun indexOf(element: T): Int {
        val endIndex = fromIndex + size
        for (i in fromIndex until endIndex) {
            if (elements[i] == element) {
                return i
            }
        }
        return -1
    }

    override fun lastIndexOf(element: T): Int {
        val startIndex = fromIndex + size
        for (i in startIndex downTo fromIndex) {
            if (elements[i] == element) {
                return i
            }
        }
        return -1
    }

    override operator fun iterator() = ObservableSubListIterator(this)

    override fun listIterator() = ObservableSubListIterator(this)

    override fun listIterator(index: Int) = ObservableSubListIterator(this, index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        return parentList.subList(parentIndex(fromIndex), parentIndex(toIndex))
    }

    /** @return a read-only copy that will NOT mutate if the backing list mutates. */
    override fun copy() = ObservableList(elements, elementHandler)

    /** @return a mutable copy that will NOT mutate if the backing list mutates. */
    fun mutableCopy() = MutableObservableList(elements, elementHandler)

    /** @return a read-only version of [parentList] that will NOT mutate if the parent list mutates */
    fun getParentCopy(): ObservableList<T> = parentList.copy()

    /** @return a read-only version of [parentList] that will NOT mutate if the parent list mutates */
    fun getParentMutableCopy(): MutableObservableList<T> = parentList.mutableCopy()

    /** @return a read-only version of [parentList] that WILL mutate if the backing list mutates */
    fun getParent(): ObservableList<T> = parentList
}
