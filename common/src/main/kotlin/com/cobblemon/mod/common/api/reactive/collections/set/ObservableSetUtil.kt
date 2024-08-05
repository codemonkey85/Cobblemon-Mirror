package com.cobblemon.mod.common.api.reactive.collections.set

import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.collections.getElementObservables


fun <T> Collection<T>.toObservableSet(
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = ObservableSet(this, elementHandler)


fun <T> Collection<T>.toMutableObservableSet(
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
) = MutableObservableSet(this, elementHandler)


fun <T> observableSetOf(
    vararg elements: T,
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): ObservableSet<T> = ObservableSet(elements.toSet(), elementHandler)


fun <T> mutableObservableSetOf(
    vararg elements: T,
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): MutableObservableSet<T> = MutableObservableSet(elements.toSet(), elementHandler)


fun <T> observableSetOf(
    elements: Collection<T> = setOf(),
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): ObservableSet<T> = ObservableSet(elements, elementHandler)


fun <T> mutableObservableSetOf(
    elements: Collection<T> = setOf(),
    elementHandler: (T) -> Set<Observable<*>> = { it.getElementObservables() },
): MutableObservableSet<T> = MutableObservableSet(elements, elementHandler)
