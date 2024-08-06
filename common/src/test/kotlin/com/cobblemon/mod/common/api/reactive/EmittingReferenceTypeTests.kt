package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.list.MutableObservableList
import com.cobblemon.mod.common.api.reactive.collections.map.MutableObservableMap
import com.cobblemon.mod.common.api.reactive.collections.set.MutableObservableSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object EmittingReferenceTypeTests {

    @Test
    fun setElementsEmittingChange() = elementsEmittingChange(MutableObservableSet())

    @Test
    fun listElementsEmittingChange() = elementsEmittingChange(MutableObservableList())

    fun <C : Collection<SettableObservable<Int>>> elementsEmittingChange(
        mutableObservableCollection: MutableObservableCollection<SettableObservable<Int>, C>
    ) {
        val getNew = { SettableObservable(0) }
        val addNew = { c: MutableCollection<SettableObservable<Int>>, count: Int ->
            for (i in 0 until count) {
                c.add(getNew())
            }
        }

        addNew(mutableObservableCollection, 4)

        val observableCollection = mutableObservableCollection.copy()

        var anyChangeTally = 0
        var subscription = observableCollection.subscribe { anyChangeTally++ }

        var mutableAnyChangeTally = 0
        var mutableSubscription = mutableObservableCollection.subscribe { mutableAnyChangeTally++ }

        val incrementValues = { iterations: Int ->
            for (i in 0 until iterations) {
                mutableObservableCollection.forEach { it.set(it.get() + 1) }
            }
            iterations * observableCollection.size
        }

        var newEmits = incrementValues(1)
        var expectedTally = newEmits
        var mutableExpectedTally = newEmits

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        mutableSubscription.unsubscribe()

        newEmits = incrementValues(1)
        expectedTally += newEmits

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        mutableSubscription = mutableObservableCollection.subscribe { mutableAnyChangeTally++ }

        newEmits = incrementValues(1)
        expectedTally += newEmits
        mutableExpectedTally += newEmits

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        addNew(mutableObservableCollection, 4)

        mutableExpectedTally += 4
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        newEmits = incrementValues(1)
        expectedTally += newEmits
        mutableExpectedTally += newEmits + (mutableObservableCollection.size - observableCollection.size)

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        mutableExpectedTally += mutableObservableCollection.size
        mutableObservableCollection.clear()

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        incrementValues(1)

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)
    }

    @Test
    fun subListElementsEmittingChange() {
        val getNew = { SettableObservable(0) }
        val addNew = { list: MutableObservableList<SettableObservable<Int>>, count: Int ->
            for (i in 0 until count) {
                list.add(getNew())
            }
        }

        val mutableObservableList = MutableObservableList<SettableObservable<Int>>()
        addNew(mutableObservableList, 4)

        val subList = mutableObservableList.subList(1, 3)

        var anyChangeTally = 0
        var subListAnyChangeTally = 0

        val listSub = mutableObservableList.subscribe { anyChangeTally++ }
        val subListSub = subList.subscribe { subListAnyChangeTally++ }

        val originalZeroElement = mutableObservableList[0]

        originalZeroElement.set(originalZeroElement.get() + 1)
        var expectedTally = 1
        var expectedSubListTally = 0

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)

        // originalZeroElement pushed into of subList window

        mutableObservableList.add(0, getNew())
        expectedTally += 1
        expectedSubListTally += 2

        originalZeroElement.set(originalZeroElement.get() + 1)
        expectedTally += 1
        expectedSubListTally += 1

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)


        mutableObservableList.add(0, getNew())
        expectedTally += 1
        expectedSubListTally += 2

        originalZeroElement.set(originalZeroElement.get() + 1)
        expectedTally += 1
        expectedSubListTally += 1

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)

        // originalZeroElement pushed out of subList window

        mutableObservableList.add(0, getNew())
        expectedTally += 1
        expectedSubListTally += 2

        originalZeroElement.set(originalZeroElement.get() + 1)
        expectedTally += 1
        expectedSubListTally += 0

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)

        // originalZeroElement drops back into subList window

        mutableObservableList.removeAt(0)
        expectedTally += 1
        expectedSubListTally += 2

        originalZeroElement.set(originalZeroElement.get() + 1)
        expectedTally += 1
        expectedSubListTally += 1

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)


        mutableObservableList.removeAt(0)
        expectedTally += 1
        expectedSubListTally += 2

        originalZeroElement.set(originalZeroElement.get() + 1)
        expectedTally += 1
        expectedSubListTally += 1

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)

        // originalZeroElement drops out of subList window

        mutableObservableList.removeAt(0)
        expectedTally += 1
        expectedSubListTally += 2

        originalZeroElement.set(originalZeroElement.get() + 1)
        expectedTally += 1
        expectedSubListTally += 0

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(subListAnyChangeTally == expectedSubListTally)
    }

    @Test
    fun entriesEmittingChange() {
        val getNew = { SettableObservable(0) }
        val putNew = {
                map: MutableObservableMap<SettableObservable<Int>, SettableObservable<Int>>,
                count: Int,
            ->
            for (i in 0 until count) {
                map.put(getNew(), getNew())
            }
        }

        val mutableObservableMap = MutableObservableMap<SettableObservable<Int>, SettableObservable<Int>>()

        putNew(mutableObservableMap, 4)

        val observableMap = mutableObservableMap.copy()

        var anyChangeTally = 0
        var subscription = observableMap.subscribe { anyChangeTally++ }

        var mutableAnyChangeTally = 0
        var mutableSubscription = mutableObservableMap.subscribe { mutableAnyChangeTally++ }

        val incrementValues = { iterations: Int ->
            for (i in 0 until iterations) {
                mutableObservableMap.forEach { (key, value) ->
                    key.set(key.get() + 1)
                    value.set(value.get() + 1)
                }
            }
            iterations * observableMap.size * 2
        }

        var newEmits = incrementValues(1)
        var expectedTally = newEmits
        var mutableExpectedTally = newEmits

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        mutableSubscription.unsubscribe()

        newEmits = incrementValues(1)
        expectedTally += newEmits

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        mutableSubscription = mutableObservableMap.subscribe { mutableAnyChangeTally++ }

        newEmits = incrementValues(1)
        expectedTally += newEmits
        mutableExpectedTally += newEmits

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        putNew(mutableObservableMap, 4)

        mutableExpectedTally += 4
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        newEmits = incrementValues(1)
        expectedTally += newEmits
        mutableExpectedTally += newEmits + (2 * (mutableObservableMap.size - observableMap.size))

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        mutableExpectedTally += mutableObservableMap.size
        mutableObservableMap.clear()

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)

        incrementValues(1)

        Assertions.assertTrue(anyChangeTally == expectedTally)
        Assertions.assertTrue(mutableAnyChangeTally == mutableExpectedTally)
    }
}