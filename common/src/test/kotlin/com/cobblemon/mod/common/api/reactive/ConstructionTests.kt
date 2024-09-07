package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.list.*
import com.cobblemon.mod.common.api.reactive.collections.map.*
import com.cobblemon.mod.common.api.reactive.collections.set.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

object ConstructionTests {

    @Test
    fun setConstruction() {
        val sets = TestSets.default()

        val inputSet = sets.input
        val observableSet = sets.observable
        val mutableObservableSet = sets.mutableObservable
        val emptyObservableSet = sets.emptyObservable
        val emptyMutableObservableSet = sets.emptyMutableObservable

        Assertions.assertTrue(observableSet.containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSet.containsAll(inputSet))
        Assertions.assertTrue(emptyObservableSet.isEmpty())
        Assertions.assertTrue(emptyMutableObservableSet.isEmpty())

        Assertions.assertTrue(ObservableSet(inputSet).containsAll(inputSet))
        Assertions.assertTrue(ObservableSet(observableSet).containsAll(inputSet))
        Assertions.assertTrue(ObservableSet(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(ObservableSet(emptyObservableSet).isEmpty())
        Assertions.assertTrue(ObservableSet(emptyMutableObservableSet).isEmpty())
        Assertions.assertTrue(ObservableSet<String>(inputSet).containsAll(inputSet))
        Assertions.assertTrue(ObservableSet<String>(observableSet).containsAll(inputSet))
        Assertions.assertTrue(ObservableSet<String>(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(ObservableSet<String>(emptyObservableSet).isEmpty())
        Assertions.assertTrue(ObservableSet<String>(emptyMutableObservableSet).isEmpty())

        Assertions.assertTrue(MutableObservableSet(inputSet).containsAll(inputSet))
        Assertions.assertTrue(MutableObservableSet(observableSet).containsAll(inputSet))
        Assertions.assertTrue(MutableObservableSet(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(MutableObservableSet(emptyObservableSet).isEmpty())
        Assertions.assertTrue(MutableObservableSet(emptyMutableObservableSet).isEmpty())
        Assertions.assertTrue(MutableObservableSet<String>(inputSet).containsAll(inputSet))
        Assertions.assertTrue(MutableObservableSet<String>(observableSet).containsAll(inputSet))
        Assertions.assertTrue(MutableObservableSet<String>(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(MutableObservableSet<String>(emptyObservableSet).isEmpty())
        Assertions.assertTrue(MutableObservableSet<String>(emptyMutableObservableSet).isEmpty())

        Assertions.assertTrue(inputSet.toObservableSet().containsAll(inputSet))
        Assertions.assertTrue(observableSet.toObservableSet().containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSet.toObservableSet().containsAll(inputSet))
        Assertions.assertTrue(emptyObservableSet.toObservableSet().isEmpty())
        Assertions.assertTrue(emptyMutableObservableSet.toObservableSet().isEmpty())
        Assertions.assertTrue(inputSet.toObservableSet<String>().containsAll(inputSet))
        Assertions.assertTrue(observableSet.toObservableSet<String>().containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSet.toObservableSet<String>().containsAll(inputSet))
        Assertions.assertTrue(emptyObservableSet.toObservableSet<String>().isEmpty())
        Assertions.assertTrue(emptyMutableObservableSet.toObservableSet<String>().isEmpty())

        Assertions.assertTrue(inputSet.toMutableObservableSet().containsAll(inputSet))
        Assertions.assertTrue(observableSet.toMutableObservableSet().containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSet.toMutableObservableSet().containsAll(inputSet))
        Assertions.assertTrue(emptyObservableSet.toMutableObservableSet().isEmpty())
        Assertions.assertTrue(emptyMutableObservableSet.toMutableObservableSet().isEmpty())
        Assertions.assertTrue(inputSet.toMutableObservableSet<String>().containsAll(inputSet))
        Assertions.assertTrue(observableSet.toMutableObservableSet<String>().containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSet.toMutableObservableSet<String>().containsAll(inputSet))
        Assertions.assertTrue(emptyObservableSet.toMutableObservableSet<String>().isEmpty())
        Assertions.assertTrue(emptyMutableObservableSet.toMutableObservableSet<String>().isEmpty())

        Assertions.assertTrue(observableSetOf(inputSet).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf(observableSet).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf(emptyObservableSet).isEmpty())
        Assertions.assertTrue(observableSetOf(emptyMutableObservableSet).isEmpty())
        Assertions.assertTrue(observableSetOf<String>(inputSet).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf<String>(observableSet).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf<String>(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf<String>(emptyObservableSet).isEmpty())
        Assertions.assertTrue(observableSetOf<String>(emptyMutableObservableSet).isEmpty())

        Assertions.assertTrue(mutableObservableSetOf(inputSet).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf(observableSet).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf(emptyObservableSet).isEmpty())
        Assertions.assertTrue(mutableObservableSetOf(emptyMutableObservableSet).isEmpty())
        Assertions.assertTrue(mutableObservableSetOf<String>(inputSet).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf<String>(observableSet).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf<String>(mutableObservableSet).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf<String>(emptyObservableSet).isEmpty())
        Assertions.assertTrue(mutableObservableSetOf<String>(emptyMutableObservableSet).isEmpty())

        val ordered = inputSet.toList()
        Assertions.assertTrue(observableSetOf(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf<String>(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf<String>(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(observableSetOf<String>(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))

        Assertions.assertTrue(mutableObservableSetOf(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf<String>(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf<String>(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
        Assertions.assertTrue(mutableObservableSetOf<String>(ordered[0], ordered[1], ordered[2], ordered[3]).containsAll(inputSet))
    }

    @Test
    fun listConstruction() {
        val lists = TestLists.default()
        val input = lists.input
        val observable = lists.observable
        val mutableObservable = lists.mutableObservable
        val emptyObservable = lists.emptyObservable
        val emptyMutableObservable = lists.emptyMutableObservable

        Assertions.assertTrue(observable.containsAll(input))
        Assertions.assertTrue(mutableObservable.containsAll(input))
        Assertions.assertTrue(emptyObservable.isEmpty())
        Assertions.assertTrue(emptyMutableObservable.isEmpty())

        Assertions.assertTrue(ObservableList(input).containsAll(input))
        Assertions.assertTrue(ObservableList(observable).containsAll(input))
        Assertions.assertTrue(ObservableList(mutableObservable).containsAll(input))
        Assertions.assertTrue(ObservableList(emptyObservable).isEmpty())
        Assertions.assertTrue(ObservableList(emptyMutableObservable).isEmpty())
        Assertions.assertTrue(ObservableList<String>(input).containsAll(input))
        Assertions.assertTrue(ObservableList<String>(observable).containsAll(input))
        Assertions.assertTrue(ObservableList<String>(mutableObservable).containsAll(input))
        Assertions.assertTrue(ObservableList<String>(emptyObservable).isEmpty())
        Assertions.assertTrue(ObservableList<String>(emptyMutableObservable).isEmpty())

        Assertions.assertTrue(MutableObservableList(input).containsAll(input))
        Assertions.assertTrue(MutableObservableList(observable).containsAll(input))
        Assertions.assertTrue(MutableObservableList(mutableObservable).containsAll(input))
        Assertions.assertTrue(MutableObservableList(emptyObservable).isEmpty())
        Assertions.assertTrue(MutableObservableList(emptyMutableObservable).isEmpty())
        Assertions.assertTrue(MutableObservableList<String>(input).containsAll(input))
        Assertions.assertTrue(MutableObservableList<String>(observable).containsAll(input))
        Assertions.assertTrue(MutableObservableList<String>(mutableObservable).containsAll(input))
        Assertions.assertTrue(MutableObservableList<String>(emptyObservable).isEmpty())
        Assertions.assertTrue(MutableObservableList<String>(emptyMutableObservable).isEmpty())

        Assertions.assertTrue(input.toObservableList().containsAll(input))
        Assertions.assertTrue(observable.toObservableList().containsAll(input))
        Assertions.assertTrue(mutableObservable.toObservableList().containsAll(input))
        Assertions.assertTrue(emptyObservable.toObservableList().isEmpty())
        Assertions.assertTrue(emptyMutableObservable.toObservableList().isEmpty())
        Assertions.assertTrue(input.toObservableList<String>().containsAll(input))
        Assertions.assertTrue(observable.toObservableList<String>().containsAll(input))
        Assertions.assertTrue(mutableObservable.toObservableList<String>().containsAll(input))
        Assertions.assertTrue(emptyObservable.toObservableList<String>().isEmpty())
        Assertions.assertTrue(emptyMutableObservable.toObservableList<String>().isEmpty())

        Assertions.assertTrue(input.toMutableObservableList().containsAll(input))
        Assertions.assertTrue(observable.toMutableObservableList().containsAll(input))
        Assertions.assertTrue(mutableObservable.toMutableObservableList().containsAll(input))
        Assertions.assertTrue(emptyObservable.toMutableObservableList().isEmpty())
        Assertions.assertTrue(emptyMutableObservable.toMutableObservableList().isEmpty())
        Assertions.assertTrue(input.toMutableObservableList<String>().containsAll(input))
        Assertions.assertTrue(observable.toMutableObservableList<String>().containsAll(input))
        Assertions.assertTrue(mutableObservable.toMutableObservableList<String>().containsAll(input))
        Assertions.assertTrue(emptyObservable.toMutableObservableList<String>().isEmpty())
        Assertions.assertTrue(emptyMutableObservable.toMutableObservableList<String>().isEmpty())

        Assertions.assertTrue(observableListOf(input).containsAll(input))
        Assertions.assertTrue(observableListOf(observable).containsAll(input))
        Assertions.assertTrue(observableListOf(mutableObservable).containsAll(input))
        Assertions.assertTrue(observableListOf(emptyObservable).isEmpty())
        Assertions.assertTrue(observableListOf(emptyMutableObservable).isEmpty())
        Assertions.assertTrue(observableListOf<String>(input).containsAll(input))
        Assertions.assertTrue(observableListOf<String>(observable).containsAll(input))
        Assertions.assertTrue(observableListOf<String>(mutableObservable).containsAll(input))
        Assertions.assertTrue(observableListOf<String>(emptyObservable).isEmpty())
        Assertions.assertTrue(observableListOf<String>(emptyMutableObservable).isEmpty())

        Assertions.assertTrue(mutableObservableListOf(input).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf(observable).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf(mutableObservable).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf(emptyObservable).isEmpty())
        Assertions.assertTrue(mutableObservableListOf(emptyMutableObservable).isEmpty())
        Assertions.assertTrue(mutableObservableListOf<String>(input).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf<String>(observable).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf<String>(mutableObservable).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf<String>(emptyObservable).isEmpty())
        Assertions.assertTrue(mutableObservableListOf<String>(emptyMutableObservable).isEmpty())

        Assertions.assertTrue(observableListOf(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(observableListOf(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(observableListOf(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(observableListOf<String>(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(observableListOf<String>(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(observableListOf<String>(input[0], input[1], input[2], input[3]).containsAll(input))

        Assertions.assertTrue(mutableObservableListOf(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf<String>(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf<String>(input[0], input[1], input[2], input[3]).containsAll(input))
        Assertions.assertTrue(mutableObservableListOf<String>(input[0], input[1], input[2], input[3]).containsAll(input))

    }

    @Test
    fun subListConstruction() {
        val lists = TestLists.default()
        val inputSubList = lists.input.subList(1, 3)
        val inputSubSubList = inputSubList.subList(0, 1)
        val subList = lists.observable.subList(1, 3)
        val subSubList = subList.subList(0, 1)
        val mutableObservableSubList = lists.mutableObservable.subList(1, 3)
        val mutableObservableSubSubList = mutableObservableSubList.subList(0, 1)

        Assertions.assertTrue(subList.containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableSubList.containsAll(inputSubList))

        Assertions.assertTrue(ObservableList(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(ObservableList(subList).containsAll(inputSubList))
        Assertions.assertTrue(ObservableList(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(ObservableList(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(ObservableList(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(ObservableList(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(ObservableList<String>(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(ObservableList<String>(subList).containsAll(inputSubList))
        Assertions.assertTrue(ObservableList<String>(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(ObservableList<String>(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(ObservableList<String>(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(ObservableList<String>(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(MutableObservableList(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(MutableObservableList(subList).containsAll(inputSubList))
        Assertions.assertTrue(MutableObservableList(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(MutableObservableList(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(MutableObservableList(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(MutableObservableList(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(MutableObservableList<String>(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(MutableObservableList<String>(subList).containsAll(inputSubList))
        Assertions.assertTrue(MutableObservableList<String>(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(MutableObservableList<String>(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(MutableObservableList<String>(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(MutableObservableList<String>(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(observableListOf(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(observableListOf(subList).containsAll(inputSubList))
        Assertions.assertTrue(observableListOf(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(observableListOf(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(observableListOf(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(observableListOf(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(observableListOf<String>(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(observableListOf<String>(subList).containsAll(inputSubList))
        Assertions.assertTrue(observableListOf<String>(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(observableListOf<String>(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(observableListOf<String>(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(observableListOf<String>(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(mutableObservableListOf(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableListOf(subList).containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableListOf(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableListOf(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableListOf(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableListOf(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(mutableObservableListOf<String>(inputSubList).containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableListOf<String>(subList).containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableListOf<String>(mutableObservableSubList).containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableListOf<String>(inputSubSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableListOf<String>(subSubList).containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableListOf<String>(mutableObservableSubSubList).containsAll(inputSubSubList))

        Assertions.assertTrue(inputSubList.toObservableList().containsAll(inputSubList))
        Assertions.assertTrue(subList.toObservableList().containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableSubList.toObservableList().containsAll(inputSubList))
        Assertions.assertTrue(inputSubSubList.toObservableList().containsAll(inputSubSubList))
        Assertions.assertTrue(subSubList.toObservableList().containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableSubSubList.toObservableList().containsAll(inputSubSubList))

        Assertions.assertTrue(inputSubList.toObservableList<String>().containsAll(inputSubList))
        Assertions.assertTrue(subList.toObservableList<String>().containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableSubList.toObservableList<String>().containsAll(inputSubList))
        Assertions.assertTrue(inputSubSubList.toObservableList<String>().containsAll(inputSubSubList))
        Assertions.assertTrue(subSubList.toObservableList<String>().containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableSubSubList.toObservableList<String>().containsAll(inputSubSubList))

        Assertions.assertTrue(inputSubList.toMutableObservableList().containsAll(inputSubList))
        Assertions.assertTrue(subList.toMutableObservableList().containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableSubList.toMutableObservableList().containsAll(inputSubList))
        Assertions.assertTrue(inputSubSubList.toMutableObservableList().containsAll(inputSubSubList))
        Assertions.assertTrue(subSubList.toMutableObservableList().containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableSubSubList.toMutableObservableList().containsAll(inputSubSubList))

        Assertions.assertTrue(inputSubList.toMutableObservableList<String>().containsAll(inputSubList))
        Assertions.assertTrue(subList.toMutableObservableList<String>().containsAll(inputSubList))
        Assertions.assertTrue(mutableObservableSubList.toMutableObservableList<String>().containsAll(inputSubList))
        Assertions.assertTrue(inputSubSubList.toMutableObservableList<String>().containsAll(inputSubSubList))
        Assertions.assertTrue(subSubList.toMutableObservableList<String>().containsAll(inputSubSubList))
        Assertions.assertTrue(mutableObservableSubSubList.toMutableObservableList<String>().containsAll(inputSubSubList))
    }

    @Test
    fun mapConstruction() {
        val testMaps = TestMaps.default()

        val inputMap = testMaps.input
        val map = testMaps.observable
        val mutableMap = testMaps.mutableObservable
        val emptyMap = testMaps.emptyObservable
        val emptyMutableMap = testMaps.emptyMutableObservable
        val entries = inputMap.entries.toList()
        val pairs = entries.pairs().toList()

        Assertions.assertTrue(map.containsAll(inputMap))
        Assertions.assertTrue(mutableMap.containsAll(inputMap))
        Assertions.assertTrue(emptyMap.isEmpty())
        Assertions.assertTrue(emptyMutableMap.isEmpty())

        Assertions.assertTrue(ObservableMap(inputMap).containsAll(inputMap))
        Assertions.assertTrue(ObservableMap(map).containsAll(inputMap))
        Assertions.assertTrue(ObservableMap(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(ObservableMap(emptyMap).isEmpty())
        Assertions.assertTrue(ObservableMap(emptyMutableMap).isEmpty())
        Assertions.assertTrue(ObservableMap<Int, String>(inputMap).containsAll(inputMap))
        Assertions.assertTrue(ObservableMap<Int, String>(map).containsAll(inputMap))
        Assertions.assertTrue(ObservableMap<Int, String>(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(ObservableMap<Int, String>(emptyMap).isEmpty())
        Assertions.assertTrue(ObservableMap<Int, String>(emptyMutableMap).isEmpty())

        Assertions.assertTrue(MutableObservableMap(inputMap).containsAll(inputMap))
        Assertions.assertTrue(MutableObservableMap(map).containsAll(inputMap))
        Assertions.assertTrue(MutableObservableMap(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(MutableObservableMap(emptyMap).isEmpty())
        Assertions.assertTrue(MutableObservableMap(emptyMutableMap).isEmpty())
        Assertions.assertTrue(MutableObservableMap<Int, String>(inputMap).containsAll(inputMap))
        Assertions.assertTrue(MutableObservableMap<Int, String>(map).containsAll(inputMap))
        Assertions.assertTrue(MutableObservableMap<Int, String>(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(MutableObservableMap<Int, String>(emptyMap).isEmpty())
        Assertions.assertTrue(MutableObservableMap<Int, String>(emptyMutableMap).isEmpty())

        Assertions.assertTrue(inputMap.toObservableMap().containsAll(inputMap))
        Assertions.assertTrue(map.toObservableMap().containsAll(inputMap))
        Assertions.assertTrue(mutableMap.toObservableMap().containsAll(inputMap))
        Assertions.assertTrue(pairs.toObservableMap().containsAll(inputMap))
        Assertions.assertTrue(emptyMap.toObservableMap().isEmpty())
        Assertions.assertTrue(emptyMutableMap.toObservableMap().isEmpty())
        Assertions.assertTrue(inputMap.toObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(map.toObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(mutableMap.toObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(pairs.toObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(emptyMap.toObservableMap<Int, String>().isEmpty())
        Assertions.assertTrue(emptyMutableMap.toObservableMap<Int, String>().isEmpty())

        Assertions.assertTrue(inputMap.toMutableObservableMap().containsAll(inputMap))
        Assertions.assertTrue(map.toMutableObservableMap().containsAll(inputMap))
        Assertions.assertTrue(mutableMap.toMutableObservableMap().containsAll(inputMap))
        Assertions.assertTrue(pairs.toMutableObservableMap().containsAll(inputMap))
        Assertions.assertTrue(emptyMap.toMutableObservableMap().isEmpty())
        Assertions.assertTrue(emptyMutableMap.toMutableObservableMap().isEmpty())
        Assertions.assertTrue(inputMap.toMutableObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(map.toMutableObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(mutableMap.toMutableObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(pairs.toMutableObservableMap<Int, String>().containsAll(inputMap))
        Assertions.assertTrue(emptyMap.toMutableObservableMap<Int, String>().isEmpty())
        Assertions.assertTrue(emptyMutableMap.toMutableObservableMap<Int, String>().isEmpty())

        Assertions.assertTrue(observableMapOf(inputMap).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf(map).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf(pairs).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf(emptyMap).isEmpty())
        Assertions.assertTrue(observableMapOf(emptyMutableMap).isEmpty())
        Assertions.assertTrue(observableMapOf<Int, String>(inputMap).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf<Int, String>(map).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf<Int, String>(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf<Int, String>(pairs).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf<Int, String>(emptyMap).isEmpty())
        Assertions.assertTrue(observableMapOf<Int, String>(emptyMutableMap).isEmpty())

        Assertions.assertTrue(mutableObservableMapOf(inputMap).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf(map).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf(pairs).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf(emptyMap).isEmpty())
        Assertions.assertTrue(mutableObservableMapOf(emptyMutableMap).isEmpty())
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(inputMap).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(map).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(mutableMap).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(pairs).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(emptyMap).isEmpty())
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(emptyMutableMap).isEmpty())

        Assertions.assertTrue(observableMapOf(entries[0], entries[1], entries[2], entries[3]).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf(pairs[0], pairs[1], pairs[2], pairs[3]).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf<Int, String>(entries[0], entries[1], entries[2], entries[3]).containsAll(inputMap))
        Assertions.assertTrue(observableMapOf<Int, String>(pairs[0], pairs[1], pairs[2], pairs[3]).containsAll(inputMap))

        Assertions.assertTrue(mutableObservableMapOf(entries[0], entries[1], entries[2], entries[3]).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf(pairs[0], pairs[1], pairs[2], pairs[3]).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(entries[0], entries[1], entries[2], entries[3]).containsAll(inputMap))
        Assertions.assertTrue(mutableObservableMapOf<Int, String>(pairs[0], pairs[1], pairs[2], pairs[3]).containsAll(inputMap))
    }
}
