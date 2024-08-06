package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.reactive.collections.list.loadMutableObservableListOf
import com.cobblemon.mod.common.api.reactive.collections.list.loadObservableListOf
import com.cobblemon.mod.common.api.reactive.collections.list.saveToNbt
import com.cobblemon.mod.common.api.reactive.collections.map.loadMutableObservableMapOf
import com.cobblemon.mod.common.api.reactive.collections.map.loadObservableMapOf
import com.cobblemon.mod.common.api.reactive.collections.map.saveToNbt
import com.cobblemon.mod.common.api.reactive.collections.set.loadMutableObservableSetOf
import com.cobblemon.mod.common.api.reactive.collections.set.loadObservableSetOf
import com.cobblemon.mod.common.api.reactive.collections.set.saveToNbt
import net.minecraft.nbt.NbtCompound
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.collections.Map.Entry

object SerializationTests {

    @Test
    fun setSerialization() {
        val sets = TestSets.default()

        val saveElementHandler = { element: String -> NbtCompound().also { it.putString("element", element) } }

        val setNbt = sets.observable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(setNbt.getInt("size") == sets.observable.size)

        val setFromNbt = setNbt.loadObservableSetOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(setFromNbt.containsAll(sets.input))

        val mutableSetNbt = sets.mutableObservable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(mutableSetNbt.getInt("size") == sets.mutableObservable.size)

        val mutableSetFromNbt = mutableSetNbt.loadMutableObservableSetOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(mutableSetFromNbt.containsAll(sets.input))

        val emptySetNbt = sets.emptyObservable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(emptySetNbt.getInt("size") == sets.emptyObservable.size)

        val emptySetFromNbt = emptySetNbt.loadObservableSetOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(emptySetFromNbt.isEmpty())

        val emptyMutableSetNbt = sets.emptyMutableObservable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(emptyMutableSetNbt.getInt("size") == sets.emptyMutableObservable.size)

        val emptyMutableSetFromNbt = emptyMutableSetNbt.loadMutableObservableSetOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(emptyMutableSetFromNbt.isEmpty())
    }

    @Test
    fun listSerialization() {
        val lists = TestLists.default()

        val saveElementHandler = { element: String -> NbtCompound().also { it.putString("element", element) } }

        val listNbt = lists.observable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(listNbt.getInt("size") == lists.observable.size)

        val listFromNbt = listNbt.loadObservableListOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(listFromNbt.containsAll(lists.input))

        val mutableListNbt = lists.mutableObservable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(mutableListNbt.getInt("size") == lists.mutableObservable.size)

        val mutableListFromNbt = mutableListNbt.loadMutableObservableListOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(mutableListFromNbt.containsAll(lists.input))

        val emptyListNbt = lists.emptyObservable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(emptyListNbt.getInt("size") == lists.emptyObservable.size)

        val emptyListFromNbt = emptyListNbt.loadObservableListOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(emptyListFromNbt.isEmpty())

        val emptyMutableListNbt = lists.emptyMutableObservable.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(emptyMutableListNbt.getInt("size") == lists.emptyMutableObservable.size)

        val emptyMutableListFromNbt = emptyMutableListNbt.loadMutableObservableListOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(emptyMutableListFromNbt.isEmpty())
    }

    @Test
    fun subListSerialization() {
        val lists = TestSubLists.default()

        val saveElementHandler = { element: String -> NbtCompound().also { it.putString("element", element) } }

        val subListNbt = lists.observableSub.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(subListNbt.getInt("size") == lists.observableSub.size)

        val subListFromNbt = subListNbt.loadObservableListOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(subListFromNbt.containsAll(lists.observableSub))

        val mutableSubListNbt = lists.mutableObservableSub.saveToNbt { saveElementHandler(it) }
        Assertions.assertTrue(mutableSubListNbt.getInt("size") == lists.mutableObservableSub.size)

        val mutableSubListFromNbt = mutableSubListNbt.loadMutableObservableListOf(
            elementKey = "element",
            loadElementHandler = { it.getString("element") },
        )
        Assertions.assertTrue(mutableSubListFromNbt.containsAll(lists.mutableObservableSub))
    }

    @Test
    fun mapSerialization() {
        val maps = TestMaps.default()

        val saveEntryHandler = { (key, value): Entry<Int, String> ->
            val entryNbt = NbtCompound()
            entryNbt.putInt("key", key)
            entryNbt.putString("value", value)
            entryNbt
        }

        val mapNbt = maps.observable.saveToNbt { saveEntryHandler(it) }
        Assertions.assertTrue(mapNbt.getInt("size") == maps.observable.size)

        val mapFromNbt = mapNbt.loadObservableMapOf(
            entryKey = "entry",
            loadEntryHandler = { it.getInt("key") to it.getString("value") },
        )
        Assertions.assertTrue(mapFromNbt.containsAll(maps.input))

        val mutableMapNbt = maps.mutableObservable.saveToNbt { saveEntryHandler(it) }
        Assertions.assertTrue(mutableMapNbt.getInt("size") == maps.mutableObservable.size)

        val mutableMapFromNbt = mutableMapNbt.loadMutableObservableMapOf(
            entryKey = "entry",
            loadEntryHandler = { it.getInt("key") to it.getString("value") },
        )
        Assertions.assertTrue(mutableMapFromNbt.containsAll(maps.input))

        val emptyMapNbt = maps.emptyObservable.saveToNbt { saveEntryHandler(it) }
        Assertions.assertTrue(emptyMapNbt.getInt("size") == maps.emptyObservable.size)

        val emptyMapFromNbt = emptyMapNbt.loadObservableMapOf(
            entryKey = "entry",
            loadEntryHandler = { it.getInt("key") to it.getString("value") },
        )
        Assertions.assertTrue(emptyMapFromNbt.isEmpty())

        val emptyMutableMapNbt = maps.emptyMutableObservable.saveToNbt { saveEntryHandler(it) }
        Assertions.assertTrue(emptyMutableMapNbt.getInt("size") == maps.emptyMutableObservable.size)

        val emptyMutableMapFromNbt = emptyMutableMapNbt.loadMutableObservableMapOf(
            entryKey = "entry",
            loadEntryHandler = { it.getInt("key") to it.getString("value") },
        )
        Assertions.assertTrue(emptyMutableMapFromNbt.isEmpty())
    }
}
