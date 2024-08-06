package com.cobblemon.mod.common.api.reactive

class TestSubLists<T>(
    input: List<T>,
    subIndices: Pair<Int, Int> = 1 to 3,
) : TestLists<T>(input) {

    val inputSub = this.input.subList(subIndices.first, subIndices.second)
    val mutableInputSub = mutableInput.subList(subIndices.first, subIndices.second)
    val observableSub = observable.subList(subIndices.first, subIndices.second)
    val mutableObservableSub = mutableObservable.subList(subIndices.first, subIndices.second)

    companion object {
        fun default() = testSubListsOf("zero", "one", "two", "three")
    }
}

fun <T> testSubListsOf(vararg elements: T) = TestSubLists(listOf(*elements))
