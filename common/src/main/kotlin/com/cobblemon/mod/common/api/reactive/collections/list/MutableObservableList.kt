package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables
import com.cobblemon.mod.common.api.reactive.collections.subscriptions.IndexedListSubscription
import com.cobblemon.mod.common.api.reactive.collections.subscriptions.ListSubscription
import com.cobblemon.mod.common.api.reactive.collections.removeIf

open class MutableObservableList<T>(
    list: Collection<T> = setOf(),
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) : MutableObservableCollection<T, List<T>>,
    ObservableList<T>(list, elementHandler),
    MutableList<T> {

    private val additionObservable = SimpleObservable<Pair<List<T>, T>>()
    private val removalObservable = SimpleObservable<Pair<List<T>, T>>()
    private val setObservable = SimpleObservable<Pair<List<T>, Pair<T, T>>>()
    private val swapObservable = SimpleObservable<Pair<List<T>, Pair<T, T>>>()
    private val additionObservableIndexed = SimpleObservable<Triple<List<T>, T, Int>>()
    private val removalObservableIndexed = SimpleObservable<Triple<List<T>, T, Int>>()
    private val setObservableIndexed = SimpleObservable<Triple<List<T>, Pair<T, T>, Int>>()
    private val swapObservableIndexed = SimpleObservable<Triple<List<T>, Pair<T, T>, Pair<Int, Int>>>()

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

    fun subscribeIndexed(
        priority: Priority,
        additionHandler: ((Triple<List<T>, T, Int>) -> Unit)?,
        removalHandler: ((Triple<List<T>, T, Int>) -> Unit)?,
        setHandler: ((Triple<List<T>, Pair<T, T>, Int>) -> Unit)?,
        swapHandler: ((Triple<List<T>, Pair<T, T>, Pair<Int, Int>>) -> Unit)?,
    ): IndexedListSubscription<T> {
        return IndexedListSubscription(
            addition = additionHandler?.let { additionObservableIndexed.subscribe(priority, it) },
            removal = removalHandler?.let { removalObservableIndexed.subscribe(priority, it) },
            set = setHandler?.let { setObservableIndexed.subscribe(priority, it) },
            swap = swapHandler?.let { swapObservableIndexed.subscribe(priority, it) },
        )
    }

    /**
     * @return `true` after handling [register] & all subscriptions to [additionObservable],
     * [additionObservableIndexed], & [ObservableList.emitAnyChange].
     */
    private fun emitAddition(element: T, index: Int): Boolean {
        register(element)
        additionObservable.emit(list to element)
        additionObservableIndexed.emit(Triple(list, element, index))
        return emitAnyChange(element)
    }

    /**
     * @return `true` after handling [unregister] & all subscriptions to [removalObservable],
     * [removalObservableIndexed] & [ObservableList.emitAnyChange].
     */
    private fun emitRemoval(element: T, index: Int): Boolean {
        unregister(element)
        removalObservable.emit(list to element)
        removalObservableIndexed.emit(Triple(list, element, index))
        return emitAnyChange(element)
    }

    /**
     * @return `true` after handling [register], [unregister] & all subscriptions to [setObservable],
     * [setObservableIndexed], & [ObservableList.emitAnyChange].
     */
    private fun emitSet(elements: Pair<T, T>, index: Int): Boolean {
        register(elements.first)
        unregister(elements.second)
        setObservable.emit(list to elements)
        setObservableIndexed.emit(Triple(list, elements, index))
        emitAnyChange(elements.first)
        emitAnyChange(elements.second)
        return true
    }

    /**
     * @return `true` after handling all subscriptions to [swapObservable],
     * [swapObservableIndexed], & [ObservableList.emitAnyChange].
     */
    private fun emitSwap(elements: Pair<T, T>, indices: Pair<Int, Int>): Boolean {
        swapObservable.emit(list to elements)
        swapObservableIndexed.emit(Triple(list, elements, indices))
        emitAnyChange(elements.first)
        emitAnyChange(elements.second)
        return true
    }

    override operator fun iterator() = MutableObservableListIterator(this)

    override fun listIterator() = MutableObservableListIterator(this)

    override fun listIterator(index: Int) = MutableObservableListIterator(this, index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableObservableSubList<T> {
        return MutableObservableSubList(this, fromIndex, toIndex, elementHandler)
    }

    override fun addIf(element: T, predicate: (List<T>) -> Boolean): Boolean {
        return if (predicate(list)) add(element) else false
    }

    override fun add(element: T): Boolean {
        list.add(element)
        return emitAddition(element, lastIndex)
    }

    fun addAtIf(index: Int, element: T, predicate: (List<T>) -> Boolean): Boolean {
        return if (predicate(list)) {
            add(index, element)
            true
        } else {
            false
        }
    }

    override fun add(index: Int, element: T) {
        list.add(index, element)
        emitAddition(element, index)
    }

    fun setIf(index: Int, element: T, predicate: (T) -> Boolean): T? {
        return  if (predicate(list[index])) set(index, element) else null
    }

    override operator fun set(index: Int, element: T): T {
        return list.set(index, element).also { emitSet(element to it, index) }
    }

    fun swapIf(indexOne: Int, indexTwo: Int, predicate: () -> Boolean): Boolean {
        return predicate().also { if (it) swap(indexOne, indexTwo) }
    }

    fun swap(indexOne: Int, indexTwo: Int) {
        val one = list[indexOne]
        val two = list.set(indexTwo, one)
        list[indexOne] = two
        emitSwap(one to two, indexOne to indexTwo)
    }

    override fun removeIf(predicate: (T) -> Boolean): Boolean = iterator().removeIf(predicate)

    override fun remove(element: T): Boolean {
        val indexOf = list.indexOfFirst { it == element }
        if (indexOf == -1) {
            return false
        }
        list.removeAt(indexOf)
        return emitRemoval(element, indexOf)
    }

    override fun removeAt(index: Int): T {
        val element = list.removeAt(index)
        emitRemoval(element, index)
        return element
    }

    fun removeAtIf(index: Int, predicate: (T) -> Boolean): T? {
        val element = list[index]
        if (predicate(element)) {
            list.removeAt(index)
            emitRemoval(element, index)
        }
        return element
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        var currentIndex = index
        elements.forEach { add(currentIndex++, it) }
        return true
    }

    override fun removeAll(elements: Collection<T>):Boolean = removeIf { elements.contains(it) }

    override fun retainAll(elements: Collection<T>):Boolean = removeIf { !elements.contains(it) }

    override fun clear() {
        while (isNotEmpty()) {
            removeAt(0)
        }
    }
}
