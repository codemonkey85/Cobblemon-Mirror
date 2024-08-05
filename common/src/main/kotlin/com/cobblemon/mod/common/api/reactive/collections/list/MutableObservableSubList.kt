package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.subscriptions.ListSubscription
import com.cobblemon.mod.common.api.reactive.collections.removeIf

class MutableObservableSubList<T>(
    private val parentList: MutableObservableList<T>,
    private val fromIndex: Int,
    private val toIndex: Int,
    private val elementHandler: (T) -> Set<Observable<*>>,
) : MutableObservableCollection<T, List<T>>,
    MutableList<T>,
    Observable<Pair<List<T>, T>> {

    private val subscriptions = PrioritizedList<ObservableSubscription<Pair<List<T>, T>>>()
    private val subscriptionMap: MutableMap<Observable<*>, ObservableSubscription<*>> = mutableMapOf()
    private val additionObservable = SimpleObservable<Pair<List<T>, T>>()
    private val removalObservable = SimpleObservable<Pair<List<T>, T>>()
    private val setObservable = SimpleObservable<Pair<List<T>, Pair<T, T>>>()
    private val swapObservable = SimpleObservable<Pair<List<T>, Pair<T, T>>>()

    override val elements: List<T>
        get() = when {
            isEmpty() -> emptyList()
            parentList.size < maxSize -> parentList.elements.subList(fromIndex, parentList.size)
            else -> parentList.elements.subList(fromIndex, toIndexInParent)
        }
    private val maxSize: Int = toIndex - fromIndex
    override val size: Int get() = when {
        isEmpty() -> 0
        parentList.size < toIndex -> parentList.lastIndex - fromIndex
        else -> toIndex - 1
    }
    /** Last index in the sub list's window, or -1 if the window is empty. */
    val lastIndex: Int get() = size - 1
    private val lastIndexInParent: Int
        get() = when {
            isEmpty() -> fromIndex
            parentList.lastIndex < toIndex ->  parentList.lastIndex
            else -> toIndex - 1
        }
    private val toIndexInParent: Int
        get() = when {
            toIndex < parentList.size ->  toIndex
            fromIndex < parentList.size -> parentList.size
            else -> fromIndex
        }

    init {
        this.elements.forEach { register(it) }
        parentList.subscribeIndexed(
            priority = Priority.NORMAL,
            additionHandler = { (_, element, index) -> emitAddition(element, index) },
            removalHandler = { (_, element, index) -> emitRemoval(element, index) },
            setHandler = { (_, elements, index) -> emitSet(elements, index) },
            swapHandler = { (_, element, index) -> emitSwap(element, index) },
        )
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

    override fun subscribe(
        priority: Priority,
        anyChangeHandler: (Pair<List<T>, T>) -> Unit,
        additionHandler: ((Pair<List<T>, T>) -> Unit)?,
        removalHandler: ((Pair<List<T>, T>) -> Unit)?,
    ): ListSubscription<T> {
        return ListSubscription(
            anyChange = this.subscribe(priority, anyChangeHandler),
            addition = additionHandler?.let { additionObservable.subscribe(priority, it) },
            removal = removalHandler?.let { removalObservable.subscribe(priority, it) },
        )
    }

    fun subscribeAndHandle(
        priority: Priority,
        anyChangeHandler: (Pair<List<T>, T>) -> Unit,
        additionHandler: ((Pair<List<T>, T>) -> Unit)?,
        removalHandler: ((Pair<List<T>, T>) -> Unit)?,
    ): ListSubscription<T> {
        elements.forEach { anyChangeHandler(this to it) }
        return subscribe(priority, anyChangeHandler, additionHandler, removalHandler)
    }

    fun subscribe(
        priority: Priority,
        anyChangeHandler: (Pair<List<T>, T>) -> Unit,
        additionHandler: ((Pair<List<T>, T>) -> Unit)?,
        removalHandler: ((Pair<List<T>, T>) -> Unit)?,
        setHandler: ((Pair<List<T>, Pair<T, T>>) -> Unit)?,
        swapHandler: ((Pair<List<T>, Pair<T, T>>) -> Unit)?,
    ): ListSubscription<T> {
        @Suppress("DuplicatedCode")
        return ListSubscription(
            anyChange = this.subscribe(priority, anyChangeHandler),
            addition = additionHandler?.let { additionObservable.subscribe(priority, it) },
            removal = removalHandler?.let { removalObservable.subscribe(priority, it) },
            set = setHandler?.let { setObservable.subscribe(priority, it) },
            swap = swapHandler?.let { swapObservable.subscribe(priority, it) },
        )
    }

    fun subscribeAndHandle(
        priority: Priority,
        anyChangeHandler: (Pair<List<T>, T>) -> Unit,
        additionHandler: ((Pair<List<T>, T>) -> Unit)?,
        removalHandler: ((Pair<List<T>, T>) -> Unit)?,
        setHandler: ((Pair<List<T>, Pair<T, T>>) -> Unit)?,
        swapHandler: ((Pair<List<T>, Pair<T, T>>) -> Unit)?,
    ): ListSubscription<T> {
        elements.forEach { anyChangeHandler(this to it) }
        return subscribe(priority, anyChangeHandler, additionHandler, removalHandler, setHandler, swapHandler)
    }

    override fun unsubscribe(subscription: ObservableSubscription<Pair<List<T>, T>>) {
        subscriptions.remove(subscription)
    }

    private fun register(element: T) {
        elementHandler(element).forEach { observable ->
            subscriptionMap[observable] = observable.subscribe { emitAnyChange(element) }
        }
    }

    private fun unregister(element: T) {
        elementHandler(element).forEach { subscriptionMap.remove(it)?.unsubscribe() }
    }

    private fun emitAnyChange(element: T): Boolean {
        subscriptions.forEach { it.handle(elements to element) }
        return true
    }

    private fun emitAddition(element: T, index: Int) {
        if (index.isInParentRelativeWindow()) {
            val elements = this.elements
            handleAddition(elements, element)
            handlePreviousLastIndex(elements)
        } else if(index < toIndex) {
            val elements = this.elements
            handleAddition(elements, parentList[fromIndex])
            handlePreviousLastIndex(elements)
        }
    }

    private fun handlePreviousLastIndex(elements: List<T>) {
        if (parentList.size > toIndex) {
            handleRemoval(elements, parentList[toIndex])
        }
    }

    private fun emitRemoval(element: T, index: Int) {
        if (index.isInParentRelativeWindow()) {
            val elements = this.elements
            handleRemoval(elements, element)
            handleNewLastIndex(elements)
        } else if(index < fromIndex && lastIndexInParent >= fromIndex - 1) {
            val elements = this.elements
            handleRemoval(elements, parentList[fromIndex - 1])
            handleNewLastIndex(elements)
        }
    }

    private fun handleNewLastIndex(elements: List<T>) {
        if (lastIndexInParent >= toIndex - 1) {
            handleAddition(elements, parentList[toIndex - 1])
        }
    }

    private fun emitSet(elementPair: Pair<T, T>, index: Int) {
        if (!index.isInParentRelativeWindow()) {
            return
        }
        register(elementPair.first)
        unregister(elementPair.second)
        setObservable.emit(elements to elementPair)
        emitAnyChange(elementPair.first)
        emitAnyChange(elementPair.second)
    }

    private fun emitSwap(elementPair: Pair<T, T>, indices: Pair<Int, Int>) {
        val firstInWindow = indices.first.isInParentRelativeWindow()
        val secondInWindow = indices.second.isInParentRelativeWindow()
        when {
            firstInWindow && !secondInWindow -> {
                val elements = this.elements
                handleRemoval(elements, elementPair.first)
                handleAddition(elements, elementPair.second)
            }
            !firstInWindow && secondInWindow -> {
                val elements = this.elements
                handleAddition(elements, elementPair.first)
                handleRemoval(elements, elementPair.second)
            }
            firstInWindow && secondInWindow -> {
                swapObservable.emit(elements to elementPair)
                emitAnyChange(elementPair.first)
                elementPair.second?.let { emitAnyChange(it) }
            }
        }
    }

    private fun handleAddition(elements: List<T>, element: T) {
        register(element)
        additionObservable.emit(elements to element)
        emitAnyChange(element)
    }

    private fun handleRemoval(elements: List<T>, element: T) {
        unregister(element)
        removalObservable.emit(elements to element)
        emitAnyChange(element)
    }

    private fun Int.isInWindow(): Boolean = this in 0 until size

    private fun Int.isInParentRelativeWindow(): Boolean = this in fromIndex until toIndexInParent

    override fun isEmpty() = parentList.lastIndex < fromIndex

    override fun isNotEmpty() = parentList.size > fromIndex

    override fun contains(element: T) = elements.contains(element)

    override fun containsAll(elements: Collection<T>) = this.elements.containsAll(elements)

    /** @throws IndexOutOfBoundsException if sublist does not contain [subListIndex]. */
    private fun parentIndex(subListIndex: Int) = throwIfOutOfBounds(subListIndex) + fromIndex

    /**
     * @throws IndexOutOfBoundsException [index] is above [lastIndex] or below [fromIndex]
     * @return [index]
     */
    private fun throwIfOutOfBounds(index: Int): Int {
        return if (index.isInWindow()) {
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

    override operator fun iterator() = MutableObservableSubListIterator(this)

    override fun listIterator() = MutableObservableSubListIterator(this)

    override fun listIterator(index: Int) = MutableObservableSubListIterator(this, index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableObservableSubList<T> {
        return parentList.subList(parentIndex(fromIndex), parentIndex(toIndex))
    }

    fun swapIf(indexOne: Int, indexTwo: Int, predicate: () -> Boolean): Boolean {
        return predicate().also { if (it) swap(indexOne, indexTwo) }
    }

    fun swap(indexOne: Int, indexTwo: Int) {
        val parentIndexOne = parentIndex(indexOne)
        val one = parentList[parentIndexOne]
        val two = parentList.set(parentIndex(indexTwo), one)
        parentList[parentIndexOne] = two
    }

    override fun addIf(element: T, predicate: (List<T>) -> Boolean): Boolean {
        return if (predicate(this)) add(element) else false
    }

    override fun add(element: T): Boolean {
        return if (parentList.lastIndex < toIndex) {
            parentList.add(element)
        } else {
            parentList.add(toIndex, element)
            true
        }
    }

    fun addAtIf(index: Int, element: T, predicate: (List<T>) -> Boolean): Boolean {
        return if (predicate(this)) {
            add(index, element)
            true
        } else {
            false
        }
    }

    override fun add(index: Int, element: T) {
        parentList.add(parentIndex(index), element)
    }

    override operator fun set(index: Int, element: T): T {
        return parentList.set(parentIndex(index), element)
    }

    override fun removeIf(predicate: (T) -> Boolean): Boolean = iterator().removeIf(predicate)

    override fun remove(element: T): Boolean {
        for (i in fromIndex until parentList.size) {
            val current = elements[i]
            if (current == element) {
                parentList.removeAt(i)
                return true
            }
        }
        return false
    }

    override fun removeAt(index: Int): T = parentList.removeAt(parentIndex(index))

    override fun addAll(elements: Collection<T>): Boolean {
        val parentIndex = if (lastIndex > -1) lastIndex + fromIndex else fromIndex
        return if (parentIndex > parentList.lastIndex) {
            parentList.addAll(elements)
        }  else {
            parentList.addAll(parentIndex, elements)
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return parentList.addAll(parentIndex(index), elements)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return iterator().removeIf { elements.contains(it) }
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        return iterator().removeIf { !elements.contains(it) }
    }

    /**
     * This implementation will remove all elements from the window dynamically. If an element
     * is removed & an element shifts into the sublist window that meets the predicate, the
     * new element will also be removed.
     *
     * Note: The other iterators including the ObservableSubListIterator will only operate on
     * the window provided before the iteration, even when the iterator is mutable.
     */
    private fun removeAllIf(predicate: (T) -> Boolean): Boolean {
        var mutated = false
        val outOfParentBounds: (Int) -> Boolean = { it == parentList.size }
        for (i in fromIndex until toIndexInParent) {
            if (outOfParentBounds(i)) {
                return mutated
            }
            while (predicate(parentList[i])) {
                parentList.removeAt(i).also { if (!mutated) mutated = true }
                if (outOfParentBounds(i)) {
                    return true
                }
            }
        }
        return mutated
    }

    override fun clear() {
        if (isNotEmpty()) {
            iterator().removeIf { true }
        }
    }

    /** @return a read-only copy that will NOT mutate if the backing list mutates. */
    override fun copy() = ObservableList(elements, elementHandler)

    /** @return a mutable copy that will NOT mutate if the backing list mutates. */
    override fun mutableCopy() = MutableObservableList(elements, elementHandler)

    /** @return a read-only copy of [parentList] that will NOT mutate if the parent list mutates */
    fun getParentCopy(): ObservableList<T> = parentList.copy()

    /** @return a read-only copy of [parentList] that will NOT mutate if the parent list mutates */
    fun getParentMutableCopy(): MutableObservableList<T> = parentList.mutableCopy()

    /** @return a read-only version of [parentList] that WILL mutate if the backing list mutates */
    fun getParent(): ObservableList<T> = parentList
}
