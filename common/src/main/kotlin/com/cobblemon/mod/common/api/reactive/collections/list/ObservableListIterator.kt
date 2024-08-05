package com.cobblemon.mod.common.api.reactive.collections.list

open class ObservableListIterator<T>(
    protected var iterator: MutableListIterator<T>,
    protected var index: Int = 0,
) : ListIterator<T> {

    protected var removedOrAddedThisIteration: Boolean = false
        set (value) { if (field != value) field = value }

    init {
        index--
    }

    override fun hasNext():Boolean = iterator.hasNext()

    override fun hasPrevious():Boolean = iterator.hasPrevious()

    override fun next(): T = iterator.next().also { updateState(indexOffset = 1) }

    override fun previous(): T = iterator.previous().also { updateState(indexOffset = -1) }

    override fun nextIndex(): Int = iterator.nextIndex()

    override fun previousIndex(): Int = iterator.previousIndex()

    private fun updateState(indexOffset: Int) {
        index += indexOffset
        removedOrAddedThisIteration = false
    }
}
