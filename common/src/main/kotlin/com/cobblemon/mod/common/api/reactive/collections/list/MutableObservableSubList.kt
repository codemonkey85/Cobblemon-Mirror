package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.subscriptions.ListSubscription
import com.cobblemon.mod.common.api.reactive.collections.removeIf

class MutableObservableSubList<T>(
    private val parentList: MutableObservableList<T>,
    private val fromIndex: Int,
    private val toIndex: Int,
    elementHandler: (T) -> Set<Observable<*>>,
) : MutableObservableCollection<T, List<T>>,
    ObservableSubList<T>(parentList, fromIndex, toIndex, elementHandler),
    MutableList<T> {

    private val additionObservable = SimpleObservable<Pair<List<T>, T>>()
    private val removalObservable = SimpleObservable<Pair<List<T>, T>>()
    private val setObservable = SimpleObservable<Pair<List<T>, Pair<T, T>>>()
    private val swapObservable = SimpleObservable<Pair<List<T>, Pair<T, T>>>()

    override val elements: List<T>
        get() = when {
            isEmpty() -> emptyList()
            parentList.size < maxSize -> parentList.elements.subList(fromIndex, parentList.size)
            else -> parentList.elements.subList(fromIndex, currentToIndex)
        }
    private val maxLastIndex: Int = toIndex - 1
    private val maxSize: Int = toIndex - fromIndex
    override val size: Int get() = when {
        isEmpty() -> 0
        parentList.size < toIndex -> parentList.lastIndex - fromIndex
        else -> maxLastIndex
    }
    /** This is the last index in the sublist window, or -1 if the window is empty. */
    val currentLastIndex: Int get() = size - 1
    /** This is either [fromIndex] or -1 if the sublist window is empty. */
    val currentFromIndex: Int get() = if (isEmpty()) -1 else fromIndex
    /**
     * If [parentList] size > [toIndex] this is the toIndex,
     * if the parentList size is > [fromIndex] this is the parentList size,
     * or this is the [currentFromIndex].
     */
    private val currentToIndex: Int
        get() = when {
            toIndex < parentList.size ->  toIndex
            fromIndex < parentList.size -> parentList.size
            else -> currentFromIndex
        }
    private val currentLastIndexInParent: Int
        get() = when {
            isEmpty() -> fromIndex
            parentList.lastIndex < toIndex ->  parentList.lastIndex
            else -> maxLastIndex
        }

    init {
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

    private fun emitAddition(element: T, index: Int) {
        if (index >= toIndex) {
            return
        }
        val elements = this.elements
        if (index >= fromIndex) {
            handleAddition(elements, element)
        } else if(parentList.lastIndex >= fromIndex) {
            handleAddition(elements, parentList[fromIndex])
        }
        if (parentList.size > toIndex) {
            handleRemoval(elements, parentList[toIndex])
        }
    }

    private fun emitRemoval(element: T, index: Int) {
        if (index >= toIndex) {
            return
        }
        val elements = this.elements
        if (index >= fromIndex) {
            handleRemoval(elements, element)
        } else if(parentList.lastIndex >= fromIndex - 1) {
            handleRemoval(elements, parentList[fromIndex - 1])
        }
        if (currentLastIndexInParent >= toIndex - 1) {
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
        val parentIndex = if (currentLastIndex > -1) currentLastIndex + fromIndex else fromIndex
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
        if (isEmpty()) {
            return false
        }
        var mutated = false
        val outOfParentBounds: (Int) -> Boolean = { it == parentList.size }
        for (i in fromIndex until currentToIndex) {
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
}
