package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.reactive.collections.MutableObservableCollection
import com.cobblemon.mod.common.api.reactive.collections.list.MutableObservableList
import com.cobblemon.mod.common.api.reactive.collections.map.mutableObservableMapOf
import com.cobblemon.mod.common.api.reactive.collections.set.MutableObservableSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object EmittingTests {

    @Test
    fun setAsCollectionEmitChange() = collectionEmitChange(MutableObservableSet())

    @Test
    fun listAsCollectionEmitChange() = collectionEmitChange(MutableObservableList())

    private fun collectionEmitChange(elements: MutableObservableCollection<String, *>) {
        var anyChangeOne = 0
        var additionOne = 0
        var removalOne = 0

        var anyChangeTwo = 0
        var additionTwo = 0
        var removalTwo = 0

        var expectedAnyChangeOne = 0
        var expectedAdditionOne = 0
        var expectedRemovalOne = 0

        var expectedAnyChangeTwo = 0
        var expectedAdditionTwo = 0
        var expectedRemovalTwo = 0

        val confirmExpected = {
            Assertions.assertTrue(anyChangeOne == expectedAnyChangeOne)
            Assertions.assertTrue(additionOne == expectedAdditionOne)
            Assertions.assertTrue(removalOne == expectedRemovalOne)
            Assertions.assertTrue(anyChangeTwo == expectedAnyChangeTwo)
            Assertions.assertTrue(additionTwo == expectedAdditionTwo)
            Assertions.assertTrue(removalTwo == expectedRemovalTwo)
        }

        val getNewSubOne = {
            elements.subscribe(
                anyChangeHandler = { anyChangeOne++ },
                additionHandler = { additionOne++ },
                removalHandler = { removalOne++ },
            )
        }

        val getNewSubTwo = {
            elements.subscribe(
                anyChangeHandler = { anyChangeTwo++ },
                additionHandler = { additionTwo++ },
                removalHandler = { removalTwo++ },
            )
        }

        val subscriptionOne = getNewSubOne()
        val subscriptionTwo = getNewSubTwo()

        var subbedAnyChangeOne = true
        var subbedAdditionOne = true
        var subbedRemovalOne = true

        var subbedAnyChangeTwo = true
        var subbedAdditionTwo = true
        var subbedRemovalTwo = true

        val incrementExpectedAddition = { value: Int ->
            if (subbedAnyChangeOne) expectedAnyChangeOne += value
            if (subbedAdditionOne) expectedAdditionOne += value
            if (subbedAnyChangeTwo) expectedAnyChangeTwo += value
            if (subbedAdditionTwo) expectedAdditionTwo += value
        }
        val incrementExpectedRemoval = { value: Int ->
            if (subbedAnyChangeOne) expectedAnyChangeOne += value
            if (subbedRemovalOne) expectedRemovalOne += value
            if (subbedAnyChangeTwo) expectedAnyChangeTwo += value
            if (subbedRemovalTwo) expectedRemovalTwo += value
        }

        var currentCount = 0
        val next = { "${currentCount++}" }
        val bulkInput = listOf(next(), next(), next(), next())

        val addNew = { iterations: Int ->
            for (i in 0 until iterations) {
                if (elements.add(next())) incrementExpectedAddition(1)
            }
            confirmExpected()
        }

        val addDuplicates = { iterations: Int ->
            elements.firstOrNull() ?. let{ dupe ->
                for (i in 0 until iterations) {
                    if (elements.add(dupe)) incrementExpectedAddition(1)
                }
            }
            confirmExpected()
        }

        val addNewIfTrue = { iterations: Int ->
            for (i in 0 until iterations) {
                if (elements.addIf(next()) { true }) incrementExpectedAddition(1)
            }
            confirmExpected()
        }

        val addNewIfFalse = { iterations: Int ->
            for (i in 0 until iterations) {
                if (elements.addIf(next()) { false }) incrementExpectedAddition(1)
            }
            confirmExpected()
        }

        val addDupeIfTrue = { iterations: Int ->
            elements.firstOrNull() ?. let{ dupe ->
                for (i in 0 until iterations) {
                    if (elements.addIf(dupe) { true }) incrementExpectedAddition(1)
                }
            }
            confirmExpected()
        }

        val addDupeIfFalse = { iterations: Int ->
            elements.firstOrNull() ?. let{ dupe ->
                for (i in 0 until iterations) {
                    if (elements.addIf(dupe) { false }) incrementExpectedAddition(1)
                }
            }
            confirmExpected()
        }

        val remove = { iterations: Int ->
            for (i in 0 until iterations) {
                elements.firstOrNull()?.let { if (elements.remove(it)) incrementExpectedRemoval(1) }
            }
            confirmExpected()
        }

        val removeNotContained = { iterations: Int ->
            for (i in 0 until iterations) {
                if (elements.remove("I am not here...")) incrementExpectedRemoval(1)
            }
            confirmExpected()
        }

        val removeDupeIfTrue = { iterations: Int ->
            elements.firstOrNull() ?. let{ dupe ->
                for (i in 0 until iterations) {
                    val size = elements.size
                    elements.removeIf { it == dupe }
                    val change = size - elements.size
                    if (change > 0) incrementExpectedRemoval(change)
                }
            }
            confirmExpected()
        }

        val removeDupeIfFalse = { iterations: Int ->
            elements.firstOrNull() ?. let{ dupe ->
                for (i in 0 until iterations) {
                    if (elements.removeIf { false }) incrementExpectedRemoval(1)
                }
            }
            confirmExpected()
        }

        val addAll = { iterations: Int ->
            for (i in 0 until iterations) {
                val size = elements.size
                if (elements.addAll(bulkInput)) incrementExpectedAddition(elements.size - size)
            }
            confirmExpected()
        }

        val removeAll = { iterations: Int ->
            for (i in 0 until iterations) {
                val size = elements.size
                if (elements.removeAll(bulkInput)) incrementExpectedRemoval(size - elements.size)
            }
            confirmExpected()
        }

        val retainAll = { iterations: Int ->
            for (i in 0 until iterations) {
                val size = elements.size
                if (elements.retainAll(bulkInput)) incrementExpectedRemoval(size - elements.size)
            }
            confirmExpected()
        }

        val clear = { iterations: Int ->
            for (i in 0 until iterations) {
                val size = elements.size
                elements.clear()
                val change = size - elements.size
                if (change > 0) incrementExpectedRemoval(change)
            }
            confirmExpected()
        }

        val runAll = { iterations: Int ->
            addNew(iterations)
            addDuplicates(iterations)
            addNewIfTrue(iterations)
            addNewIfFalse(iterations)
            addDupeIfTrue(iterations)
            addDupeIfFalse(iterations)
            remove(iterations)
            removeNotContained(iterations)
            removeDupeIfTrue(iterations)
            removeDupeIfFalse(iterations)
            addAll(iterations)
            removeAll(iterations)
            retainAll(iterations)
            clear(iterations)
        }

        runAll(8)

        subscriptionOne.unsubscribe()
        subbedAnyChangeOne = false
        subbedAdditionOne = false
        subbedRemovalOne = false
        runAll(8)

        subscriptionTwo.anyChange.unsubscribe()
        subbedAnyChangeTwo = false
        runAll(8)

        subscriptionTwo.addition?.unsubscribe()
        subscriptionTwo.removal?.unsubscribe()
        subbedAdditionTwo = false
        subbedRemovalTwo = false
        runAll(8)

        val newSubOne = getNewSubOne()
        newSubOne.addition?.unsubscribe()
        newSubOne.removal?.unsubscribe()
        subbedAnyChangeOne = true
        runAll(8)

        val newSubTwo = getNewSubTwo()
        newSubTwo.addition?.unsubscribe()
        newSubTwo.removal?.unsubscribe()
        subbedAnyChangeTwo = true
        runAll(8)
    }

    @Test
    fun listAndSubListEmitChange() {
        var anyChangeList = 0
        var additionList = 0
        var removalList = 0
        var setList = 0
        var swapList = 0
        var anyChangeSubList = 0
        var additionSubList = 0
        var removalSubList = 0
        var setSubList = 0
        var swapSubList = 0

        var expectedAnyChangeList = 0
        var expectedAdditionList = 0
        var expectedRemovalList = 0
        var expectedSetList = 0
        var expectedSwapList = 0
        var expectedAnyChangeSubList = 0
        var expectedAdditionSubList = 0
        var expectedRemovalSubList = 0
        var expectedSetSubList = 0
        var expectedSwapSubList = 0

        val input = listOf("zero", "one", "two", "three")
        val list = MutableObservableList(input)
        val fromIndex = 1
        val toIndex = 3
        val subList = list.subList(fromIndex, toIndex)
        val isInWindow: Int.() -> Boolean = { this in fromIndex until toIndex }

        val listSub = list.subscribe(
            priority = Priority.NORMAL,
            anyChangeHandler = { anyChangeList++ },
            additionHandler = { additionList++ },
            removalHandler = { removalList++ },
            setHandler = { setList++ },
            swapHandler = { swapList++ },
        )
        val subListSub = subList.subscribe(
            priority = Priority.NORMAL,
            anyChangeHandler = { anyChangeSubList++ },
            additionHandler = { additionSubList++ },
            removalHandler = { removalSubList++ },
            setHandler = { setSubList++ },
            swapHandler = { swapSubList++ },
        )

        val subscribedToAnyChange = true
        val subscribedToAddition = true
        val subscribedToRemoval = true
        val subscribedToSet = true
        val subscribedToSwap = true
        val subscribedToSubListAnyChange = true
        val subscribedToSubListAddition = true
        val subscribedToSubListRemoval = true
        val subscribedToSubListSet = true
        val subscribedToSubListSwap = true

        val incrementAddition = { count: Int, index: Int ->
            if (subscribedToAnyChange) expectedAnyChangeList += count
            if (subscribedToAddition) expectedAdditionList += count
            if (index < toIndex && fromIndex <= list.lastIndex) {
                if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count
                if (subscribedToSubListAddition) expectedAdditionSubList += count
                // removal for the elements pushed out of the sublist window
                if (toIndex <= list.lastIndex) {
                    if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count
                    if (subscribedToSubListRemoval) expectedRemovalSubList += count
                }
            }
        }

        val incrementRemoval = { count: Int, index: Int ->
            if (subscribedToAnyChange) expectedAnyChangeList += count
            if (subscribedToRemoval) expectedRemovalList += count
            if (index < toIndex) {
                if (fromIndex <= list.size) {
                    if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count
                    if (subscribedToSubListRemoval) expectedRemovalSubList += count
                }
                // addition for the elements dropping into the sublist window
                if (toIndex <= list.size) {
                    if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count
                    if (subscribedToSubListAddition) expectedAdditionSubList += count
                }
            }
        }

        val incrementSet = { count: Int, index: Int ->
            if (subscribedToAnyChange) expectedAnyChangeList += count * 2
            if (subscribedToSet) expectedSetList += count
            if (index.isInWindow()) {
                if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count * 2
                if (subscribedToSubListSet) expectedSetSubList += count
            }
        }

        val incrementSwap = { count: Int, indexOne: Int, indexTwo: Int ->
            if (subscribedToAnyChange) expectedAnyChangeList += count * 2
            if (subscribedToSwap) expectedSwapList += count
            if (indexOne.isInWindow() && indexTwo.isInWindow()) {
                if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count * 2
                if (subscribedToSubListSwap) expectedSwapSubList += count
            } else if ((!indexOne.isInWindow() && indexTwo.isInWindow()) ||
                (indexOne.isInWindow() && !indexTwo.isInWindow())) {
                if (subscribedToSubListAnyChange) expectedAnyChangeSubList += count * 2
                if (subscribedToSubListRemoval) expectedAdditionSubList += count
                if (subscribedToAnyChange) expectedRemovalSubList += count
            }
        }

        val confirmExpected = {
            Assertions.assertTrue(anyChangeList == expectedAnyChangeList)
            Assertions.assertTrue(additionList == expectedAdditionList)
            Assertions.assertTrue(removalList == expectedRemovalList)
            Assertions.assertTrue(setList == expectedSetList)
            Assertions.assertTrue(swapList == expectedSwapList)
            Assertions.assertTrue(anyChangeSubList == expectedAnyChangeSubList)
            Assertions.assertTrue(additionSubList == expectedAdditionSubList)
            Assertions.assertTrue(removalSubList == expectedRemovalSubList)
            Assertions.assertTrue(setSubList == expectedSetSubList)
            Assertions.assertTrue(swapSubList == expectedSwapSubList)
        }

        val confirmSynced: () -> Unit = {
            for (i in 0 until subList.size) {
                if (list.size > i + fromIndex) {
                    Assertions.assertTrue(subList[i] == list[i + fromIndex])
                }
            }
        }

        var currentCount = 0
        val next = { "${currentCount++}" }

        val add = { iterations: Int ->
            for (i in 0 until iterations) {
                list.add(next())
                incrementAddition(1, list.lastIndex)
            }
            confirmExpected()
            confirmSynced()
        }

        val addIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                list.addIf(next()) { bool }
                if (bool) {
                    incrementAddition(1, list.lastIndex)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val addAtStartIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.addAtIf(0, next()) { bool }
                if (bool) {
                    incrementAddition(1, 0)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val addAtEndIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.addAtIf(list.lastIndex, next()) { bool }
                if (bool) {
                    incrementAddition(1, list.lastIndex - 1)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val removeFromStart = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.firstOrNull()?.let {
                    if (list.remove(it)) {
                        incrementRemoval(1, 0)
                    }
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val removeFromEnd = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.removeLast()
                incrementRemoval(1, list.size)
            }
            confirmExpected()
            confirmSynced()
        }

        val removeAtStartIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.removeAtIf(0) { bool }
                if (bool) {
                    incrementRemoval(1, 0)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val removeAtEndIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.removeAtIf(list.lastIndex) { bool }
                if (bool) {
                    incrementRemoval(1, list.lastIndex)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val setStart = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list[0] = next()
                incrementSet(1, 0)
            }
            confirmExpected()
            confirmSynced()
        }

        val setEnd = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list[list.lastIndex] = next()
                incrementSet(1, list.lastIndex)
            }
            confirmExpected()
            confirmSynced()
        }

        val setStartIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.setIf(0, next()) { bool }
                if (bool) {
                    incrementSet(1, 0)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val setEndIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list.setIf(list.lastIndex, next()) { bool }
                if (bool) {
                    incrementSet(1, list.lastIndex)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val swapStart = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list[0] = next()
                incrementSet(1, 0)
            }
            confirmExpected()
            confirmSynced()
        }

        val swapEnd = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.isEmpty()) {
                    add(1)
                }
                list[list.lastIndex] = next()
                incrementSet(1, list.lastIndex)
            }
            confirmExpected()
            confirmSynced()
        }

        val swapStartIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.size < 2) {
                    add(2)
                }
                list.swapIf(0, 1) { bool }
                if (bool) {
                    incrementSwap(1, 0, 1)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val swapEndIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.size < 2) {
                    add(2)
                }
                list.swapIf(list.lastIndex, list.lastIndex - 1) { bool }
                if (bool) {
                    incrementSwap(1, list.lastIndex, list.lastIndex - 1)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val swapInsideOut = { iterations: Int ->
            for (i in 0 until iterations) {
                if (list.size < fromIndex) {
                    add(fromIndex + 1)
                }
                list.swap(0, fromIndex)
                incrementSwap(1, 0, fromIndex)
            }
            confirmExpected()
            confirmSynced()
        }

        val swapInsideOutIf = { iterations: Int, bool: Boolean ->
            for (i in 0 until iterations) {
                if (list.size < fromIndex) {
                    add(fromIndex + 1)
                }
                list.swapIf(0, fromIndex) { bool }
                if (bool) {
                    incrementSwap(1, 0, fromIndex)
                }
            }
            confirmExpected()
            confirmSynced()
        }

        val runAll = { startingSize: Int ->
            if (list.size < startingSize) {
                add(startingSize - list.size)
            } else if (list.size > startingSize) {
                removeFromStart(list.size - startingSize)
            }
            add(1)
            addIf(1, true)
            addIf(1, false)
            addAtStartIf(1, true)
            addAtStartIf(1, false)
            addAtEndIf(1, true)
            addAtEndIf(1, false)
            removeFromStart(1)
            removeFromEnd(1)
            removeAtStartIf(1, true)
            removeAtEndIf(1, false)
            setStart(1)
            setStartIf(1, true)
            setStartIf(1, false)
            setEnd(1)
            setEndIf(1, true)
            setEndIf(1, false)
            swapStart(1)
            swapStartIf(1, true)
            swapStartIf(1, false)
            swapEnd(1)
            swapEndIf(1, true)
            swapEndIf(1, false)
            swapInsideOut(1)
            swapInsideOutIf(1, true)
            swapInsideOutIf(1, false)
        }

        runAll(0)
        runAll(2)
        runAll(10)
    }

    @Test
    fun mapEmitChange() {
        val mutableMap = mutableObservableMapOf(TestMaps.defaultMap())
        var anyChangeOne = 0
        var additionOne = 0
        var removalOne = 0

        var anyChangeTwo = 0
        var additionTwo = 0
        var removalTwo = 0

        var expectedAnyChangeOne = 0
        var expectedAdditionOne = 0
        var expectedRemovalOne = 0

        var expectedAnyChangeTwo = 0
        var expectedAdditionTwo = 0
        var expectedRemovalTwo = 0

        val confirmExpected = {
            Assertions.assertTrue(anyChangeOne == expectedAnyChangeOne)
            Assertions.assertTrue(additionOne == expectedAdditionOne)
            Assertions.assertTrue(removalOne == expectedRemovalOne)
            Assertions.assertTrue(anyChangeTwo == expectedAnyChangeTwo)
            Assertions.assertTrue(additionTwo == expectedAdditionTwo)
            Assertions.assertTrue(removalTwo == expectedRemovalTwo)
        }

        val getNewSubOne = {
            mutableMap.subscribe(
                anyChangeHandler = { anyChangeOne++ },
                additionHandler = { additionOne++ },
                removalHandler = { removalOne++ },
            )
        }

        val getNewSubTwo = {
            mutableMap.subscribe(
                anyChangeHandler = { anyChangeTwo++ },
                additionHandler = { additionTwo++ },
                removalHandler = { removalTwo++ },
            )
        }

        val subscriptionOne = getNewSubOne()
        val subscriptionTwo = getNewSubTwo()

        var subbedAnyChangeOne = true
        var subbedAdditionOne = true
        var subbedRemovalOne = true

        var subbedAnyChangeTwo = true
        var subbedAdditionTwo = true
        var subbedRemovalTwo = true

        val incrementExpectedAddition = { value: Int ->
            if (subbedAnyChangeOne) expectedAnyChangeOne += value
            if (subbedAdditionOne) expectedAdditionOne += value
            if (subbedAnyChangeTwo) expectedAnyChangeTwo += value
            if (subbedAdditionTwo) expectedAdditionTwo += value
        }
        val incrementExpectedRemoval = { value: Int ->
            if (subbedAnyChangeOne) expectedAnyChangeOne += value
            if (subbedRemovalOne) expectedRemovalOne += value
            if (subbedAnyChangeTwo) expectedAnyChangeTwo += value
            if (subbedRemovalTwo) expectedRemovalTwo += value
        }

        var currentCount = 0
        val next = { currentCount to "${currentCount++}" }
        val bulkInput = mapOf(next(), next(), next(), next())

        val putNew = { iterations: Int ->
            for (i in 0 until iterations) {
                if (mutableMap.put(next()) != null) {
                    incrementExpectedRemoval(1)
                }
                incrementExpectedAddition(1)
            }
            confirmExpected()
        }

        val putCurrentKeys = { iterations: Int ->
            mutableMap.firstKeyOrNull()?. let { dupe ->
                for (i in 0 until iterations) {
                    if (mutableMap.put(dupe, next().second) != null) {
                        incrementExpectedRemoval(1)
                    }
                    incrementExpectedAddition(1)
                }
            }
            confirmExpected()
        }

        val removeNew = { iterations: Int ->
            for (i in 0 until iterations) {
                if (mutableMap.remove(next().first) != null) {
                    incrementExpectedRemoval(1)
                }
            }
            confirmExpected()
        }

        val removeCurrent = { iterations: Int ->
            for (i in 0 until iterations) {
                if (mutableMap.remove(next().first) != null) {
                    incrementExpectedRemoval(1)
                }
            }
            confirmExpected()
        }

        val putAll = { iterations: Int ->
            for (i in 0 until iterations) {
                var replacedCount = 0
                bulkInput.forEach { if (mutableMap.keys.contains(it.key)) replacedCount++ }
                mutableMap.putAll(bulkInput)
                incrementExpectedAddition(bulkInput.size)
                incrementExpectedRemoval(replacedCount)
            }
            confirmExpected()
        }

        val clear = { iterations: Int ->
            for (i in 0 until iterations) {
                val size = mutableMap.size
                mutableMap.clear()
                val change = size - mutableMap.size
                if (change > 0) incrementExpectedRemoval(change)
            }
            confirmExpected()
        }

        val runAll = { iterations: Int ->
            putNew(iterations)
            putCurrentKeys(iterations)
            removeNew(iterations)
            removeCurrent(iterations)
            putAll(iterations)
            clear(iterations)
        }

        runAll(8)

        subscriptionOne.unsubscribe()
        subbedAnyChangeOne = false
        subbedAdditionOne = false
        subbedRemovalOne = false
        runAll(8)

        subscriptionTwo.anyChange.unsubscribe()
        subbedAnyChangeTwo = false
        runAll(8)

        subscriptionTwo.addition?.unsubscribe()
        subscriptionTwo.removal?.unsubscribe()
        subbedAdditionTwo = false
        subbedRemovalTwo = false
        runAll(8)

        val newSubOne = getNewSubOne()
        newSubOne.addition?.unsubscribe()
        newSubOne.removal?.unsubscribe()
        subbedAnyChangeOne = true
        runAll(8)

        val newSubTwo = getNewSubTwo()
        newSubTwo.addition?.unsubscribe()
        newSubTwo.removal?.unsubscribe()
        subbedAnyChangeTwo = true
        runAll(8)
    }

}
