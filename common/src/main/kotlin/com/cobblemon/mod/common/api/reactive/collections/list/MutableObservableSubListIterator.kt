package com.cobblemon.mod.common.api.reactive.collections.list

// the name is not quite long enough yet...
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollectionIterator

open class MutableObservableSubListIterator<T>(
    subList: MutableObservableSubList<T>,
    index: Int = 0,
) : MutableObservableCollectionIterator<T, List<T>, ListIterator<T>>,
    ObservableSubListIterator<T>(subList, index),
    MutableListIterator<T> {

    private val mutableSubList: MutableObservableSubList<T> = subList

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
        mutableSubList.removeAt(--index)
        illegalSetOrRemoveState = true
    }

    override fun set(element: T) {
        throwIfIllegalState("set")
        mutableSubList[index -1] = element
    }

    override fun add(element: T) {
        when {
            subList.isEmpty() -> {
                mutableSubList.add(element)
                index = mutableSubList.currentFromIndex
            }
            index < mutableSubList.currentLastIndex -> mutableSubList.add(index++, element)
            else -> mutableSubList.add(element).run { index++ }
        }
        illegalSetOrRemoveState = true
    }

    private fun throwIfIllegalState(function: String) {
        if (illegalSetOrRemoveState == null) {
            throw IllegalStateException(
                "Observable Sub List iterator '$function' was called before 'next' or 'previous'."
            )
        } else if (illegalSetOrRemoveState == true) {
            throw IllegalStateException(
                "Observable Sub List iterator '$function' was called, " +
                        "but 'remove' or 'add' was already called this iteration."
            )
        }
    }
}
