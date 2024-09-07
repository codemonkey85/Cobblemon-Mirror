package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.removeIf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.collections.Map.Entry

object IteratorTests {

    @Test
    fun setIterator() {
        val testSets = TestSets.default()
        iteratorTest(testSets)
        bulkMutationTest(testSets)
    }


    @Test
    fun listIterator() {
        val testLists = TestLists.default()
        iteratorTest(testLists)
        bulkMutationTest(testLists)
    }

    @Test
    fun subListIterator() {
        val lists = TestSubLists.default()
        subListIteratorTest(lists)
        subListBulkMutationTest(lists)
    }

    private fun <T> subListIteratorTest(lists: TestSubLists<T>) {
        var inputSubListCount = 0
        var mutableInputSubListCount = 0
        var subListCount = 0
        var mutableSubListCount = 0

        lists.inputSub.iterator().forEach { _ -> inputSubListCount++ }
        lists.mutableInputSub.iterator().forEach { _ -> mutableInputSubListCount++ }
        lists.observableSub.iterator().forEach { _ -> subListCount++ }
        lists.mutableObservableSub.iterator().forEach { _ -> mutableSubListCount++ }

        Assertions.assertTrue(inputSubListCount == lists.inputSub.size)
        Assertions.assertTrue(mutableInputSubListCount == lists.mutableInputSub.size)
        Assertions.assertTrue(subListCount == lists.observableSub.size)
        Assertions.assertTrue(mutableSubListCount == lists.mutableObservableSub.size)
    }

    private fun <T> subListBulkMutationTest(lists: TestSubLists<T>) {
        // confirm mutations via iterator internally are tracked & reflected consistent with
        // kotlin mutable set implementations
        lists.mutableInputSub.removeAll(lists.inputSub)
        lists.mutableObservableSub.removeAll(lists.inputSub)

        Assertions.assertTrue(lists.mutableObservableSub.containsAll(lists.mutableInputSub))
        Assertions.assertTrue(lists.mutableInputSub.containsAll(lists.mutableObservableSub))

        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableInputSub))
        Assertions.assertTrue(!lists.mutableInputSub.containsAll(lists.inputSub))
        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableObservableSub))
        Assertions.assertTrue(!lists.mutableObservableSub.containsAll(lists.inputSub))

        lists.mutableInputSub.addAll(lists.inputSub)
        lists.mutableObservableSub.addAll(lists.inputSub)

        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableInputSub))
        Assertions.assertTrue(lists.mutableInputSub.containsAll(lists.inputSub))
        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableObservableSub))
        Assertions.assertTrue(lists.mutableObservableSub.containsAll(lists.inputSub))

        lists.mutableInputSub.retainAll(lists.inputSub)
        lists.mutableObservableSub.retainAll(lists.inputSub)

        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableInputSub))
        Assertions.assertTrue(lists.mutableInputSub.containsAll(lists.inputSub))
        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableObservableSub))
        Assertions.assertTrue(lists.mutableObservableSub.containsAll(lists.inputSub))
        Assertions.assertTrue(lists.mutableObservable.containsAll(lists.mutableInput))
        Assertions.assertTrue(lists.mutableObservable.size == lists.mutableInput.size)

        // back to starting lists
        lists.mutableInputSub.clear()
        lists.mutableObservableSub.clear()
        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableInputSub))
        Assertions.assertTrue(!lists.mutableInputSub.containsAll(lists.inputSub))
        Assertions.assertTrue(lists.inputSub.containsAll(lists.mutableObservableSub))
        Assertions.assertTrue(!lists.mutableObservableSub.containsAll(lists.inputSub))
    }

    private fun iteratorTest(collections: TestCollections<*, *, *>) {
        var inputCount = 0
        var mutableInputCount = 0
        var collectionCount = 0
        var mutableCollectionCount = 0
        var emptyCollectionCount = 0
        var emptyMutableCollectionCount = 0

        collections.input.iterator().forEach { _ -> inputCount++ }
        collections.mutableInput.iterator().forEach { _ -> mutableInputCount++ }
        collections.observable.iterator().forEach { _ -> collectionCount++ }
        collections.mutableObservable.iterator().forEach { _ -> mutableCollectionCount++ }
        collections.emptyObservable.iterator().forEach { _ -> emptyCollectionCount++ }
        collections.emptyMutableObservable.iterator().forEach { _ -> emptyMutableCollectionCount++ }

        Assertions.assertTrue(inputCount == collections.input.size)
        Assertions.assertTrue(mutableInputCount == collections.mutableInput.size)
        Assertions.assertTrue(collectionCount == collections.observable.size)
        Assertions.assertTrue(mutableCollectionCount == collections.mutableObservable.size)
        Assertions.assertTrue(emptyCollectionCount == collections.emptyObservable.size)
        Assertions.assertTrue(emptyMutableCollectionCount == collections.emptyMutableObservable.size)
    }

    private fun <T, C : Collection<T>, M : MutableCollection<T>> bulkMutationTest(
        collections: TestCollections<T, C, M>
    ) {
        // confirm mutations via iterator internally are tracked & reflected consistent with
        // kotlin mutable set implementations
        collections.mutableInput.removeAll(collections.input)
        collections.mutableObservable.removeAll(collections.input)
        collections.emptyMutableObservable.removeAll(collections.input)
        iteratorTest(collections)

        Assertions.assertTrue(collections.input.containsAll(collections.mutableInput))
        Assertions.assertTrue(!collections.mutableInput.containsAll(collections.input))
        Assertions.assertTrue(collections.input.containsAll(collections.mutableObservable))
        Assertions.assertTrue(!collections.mutableObservable.containsAll(collections.input))
        Assertions.assertTrue(collections.emptyMutableObservable.isEmpty())

        collections.mutableInput.addAll(collections.input)
        collections.mutableObservable.addAll(collections.input)
        collections.emptyMutableObservable.addAll(collections.input)
        iteratorTest(collections)

        Assertions.assertTrue(collections.input.containsAll(collections.mutableInput))
        Assertions.assertTrue(collections.mutableInput.containsAll(collections.input))
        Assertions.assertTrue(collections.input.containsAll(collections.mutableObservable))
        Assertions.assertTrue(collections.mutableObservable.containsAll(collections.input))
        Assertions.assertTrue(collections.emptyMutableObservable.isNotEmpty())

        collections.mutableInput.retainAll(collections.input)
        collections.mutableObservable.retainAll(collections.input)
        collections.emptyMutableObservable.retainAll(collections.input)
        iteratorTest(collections)

        Assertions.assertTrue(collections.input.containsAll(collections.mutableInput))
        Assertions.assertTrue(collections.mutableInput.containsAll(collections.input))
        Assertions.assertTrue(collections.input.containsAll(collections.mutableObservable))
        Assertions.assertTrue(collections.mutableObservable.containsAll(collections.input))
        Assertions.assertTrue(collections.emptyMutableObservable.isNotEmpty())
    }

    @Test
    fun mapIterator() {
        val testMaps = TestMaps.default()
        mapIteratorTest(testMaps)
        bulkMapMutationTest(testMaps)
    }

    private fun <K, V> mapIteratorTest(maps: TestMaps<K, V>) {
        var inputCount = 0
        var mutableInputCount = 0
        var mapCount = 0
        var mutableMapCount = 0
        var emptyMapCount = 0
        var emptyMutableMapCount = 0

        maps.input.iterator().forEach { _ -> inputCount++ }
        maps.mutableInput.iterator().forEach { _ -> mutableInputCount++ }
        maps.observable.iterator().forEach { _ -> mapCount++ }
        maps.mutableObservable.iterator().forEach { _ -> mutableMapCount++ }
        maps.emptyObservable.iterator().forEach { _ -> emptyMapCount++ }
        maps.emptyMutableObservable.iterator().forEach { _ -> emptyMutableMapCount++ }

        Assertions.assertTrue(inputCount == maps.input.size)
        Assertions.assertTrue(mutableInputCount == maps.mutableInput.size)
        Assertions.assertTrue(mapCount == maps.observable.size)
        Assertions.assertTrue(mutableMapCount == maps.mutableObservable.size)
        Assertions.assertTrue(emptyMapCount == maps.emptyObservable.size)
        Assertions.assertTrue(emptyMutableMapCount == maps.emptyMutableObservable.size)
    }

    private fun <K, V> bulkMapMutationTest(maps: TestMaps<K, V>) {
        removeIf(maps.mutableInput) { entry -> maps.input.any { match(entry, it) } }
        removeIf(maps.mutableObservable) { entry -> maps.input.any { match(entry, it) } }
        removeIf(maps.emptyMutableObservable) { entry -> maps.input.any { match(entry, it) } }
        mapIteratorTest(maps)

        Assertions.assertTrue(containsAll(maps.input, maps.mutableInput))
        Assertions.assertTrue(!containsAll(maps.mutableInput, maps.input))
        Assertions.assertTrue(containsAll(maps.input, maps.mutableObservable))
        Assertions.assertTrue(!containsAll(maps.mutableObservable, maps.input))
        Assertions.assertTrue(maps.emptyMutableObservable.isEmpty())

        maps.mutableInput.putAll(maps.input)
        maps.mutableObservable.putAll(maps.input)
        maps.emptyMutableObservable.putAll(maps.input)
        mapIteratorTest(maps)

        Assertions.assertTrue(containsAll(maps.input, maps.mutableInput))
        Assertions.assertTrue(containsAll(maps.mutableInput, maps.input))
        Assertions.assertTrue(containsAll(maps.input, maps.mutableObservable))
        Assertions.assertTrue(containsAll(maps.mutableObservable, maps.input))
        Assertions.assertTrue(maps.emptyMutableObservable.isNotEmpty())

        removeIf(maps.mutableInput) { entry -> !maps.input.any { match(entry, it) } }
        removeIf(maps.mutableObservable) { entry -> !maps.input.any { match(entry, it) } }
        removeIf(maps.emptyMutableObservable) { entry -> !maps.input.any { match(entry, it) } }
        mapIteratorTest(maps)

        Assertions.assertTrue(containsAll(maps.input, maps.mutableInput))
        Assertions.assertTrue(containsAll(maps.mutableInput, maps.input))
        Assertions.assertTrue(containsAll(maps.input, maps.mutableObservable))
        Assertions.assertTrue(containsAll(maps.mutableObservable, maps.input))
        Assertions.assertTrue(maps.emptyMutableObservable.isNotEmpty())
    }

    private fun <K, V> match(entry: Entry<K, V>, other: Entry<K, V>): Boolean {
        return entry.key == other.key && entry.value == other.value
    }

    private fun <K, V> removeIf(
        map: MutableMap<K, V>,
        predicate: (Entry<K, V>) -> Boolean,
    ) {
        map.iterator().removeIf { predicate(it) }
    }

    private fun <K, V> containsAll(map: Map<K, V>, other: Map<K, V>): Boolean {
        return when {
            other.isEmpty() -> true
            map.isEmpty() -> false
            else -> {
                map.forEach { entry ->
                    if (!other.any { it.key == entry.key && it.value == entry.value }) {
                        return false
                    }
                }
                return true
            }
        }
    }
}
