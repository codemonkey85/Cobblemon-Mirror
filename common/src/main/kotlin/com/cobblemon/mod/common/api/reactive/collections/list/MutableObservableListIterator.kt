package com.cobblemon.mod.common.api.reactive.collections.list

// the name is not quite long enough yet...
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollectionIterator

open class MutableObservableListIterator<T>(
    observableList: MutableObservableList<T>,
    index: Int = 0,
) : MutableObservableCollectionIterator<T, Set<T>, ListIterator<T>>,
    ObservableListIterator<T>(observableList, index),
    MutableListIterator<T> {

    private val mutableList: MutableObservableList<T> = observableList

    private var illegalSetOrRemoveState: Boolean? = null
        set (value) { if (field != value) field = value }

    override fun next(): T {
        illegalSetOrRemoveState = false
        return super.next()
    }

    override fun previous(): T {
        illegalSetOrRemoveState = false
        return super.previous()
    }

    override fun remove() {
        throwIfIllegalState("remove")
        mutableList.removeAt(--index)
        illegalSetOrRemoveState = true
    }

    override fun set(element: T) {
        throwIfIllegalState("set")
        mutableList[index -1] = element
    }

    override fun add(element: T) {
        when {
            mutableList.isEmpty() -> mutableList.add(element).run { index = 0 }
            index < mutableList.size -> mutableList.add(index++, element)
            else -> mutableList.add(element).run { index++ }
        }
        illegalSetOrRemoveState = true
    }

    private fun throwIfIllegalState(function: String) {
        if (illegalSetOrRemoveState == null) {
            throw IllegalStateException(
                "Observable List iterator '$function' was called before 'next' or 'previous'."
            )
        } else if (illegalSetOrRemoveState == true) {
            throw IllegalStateException(
                "Observable List iterator '$function' was called, " +
                        "but 'remove' or 'add' was already called this iteration."
            )
        }
    }
}
