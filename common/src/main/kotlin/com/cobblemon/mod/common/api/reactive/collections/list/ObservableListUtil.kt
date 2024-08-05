package com.cobblemon.mod.common.api.reactive.collections.list

import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables


fun <T> Collection<T>.toObservableList(
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = ObservableList(this, elementHandler)


fun <T> Collection<T>.toMutableObservableList(
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = MutableObservableList(this, elementHandler)


fun <T> observableListOf(
    vararg elements: T,
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): ObservableList<T> = ObservableList(elements.toList(), elementHandler)


fun <T> mutableObservableListOf(
    vararg elements: T,
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): MutableObservableList<T> = MutableObservableList(elements.toList(), elementHandler)


fun <T> observableListOf(
    elements: Collection<T> = setOf(),
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): ObservableList<T> = ObservableList(elements, elementHandler)


fun <T> mutableObservableListOf(
    elements: Collection<T> = setOf(),
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): MutableObservableList<T> = MutableObservableList(elements, elementHandler)
