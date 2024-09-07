package com.cobblemon.mod.common.api.reactive.collections

import com.cobblemon.mod.common.api.reactive.Observable


/**
 * This attempts to cast [this] value to [Observable].
 *
 * @return An observable set with [this] as an [Observable] or an empty set.
 */
fun <T> T.getElementObservables(): Set<Observable<*>> {
    this.tryGetObservable()?.let { return setOf(it) }
    return emptySet()
}


/**
 * This attempts to cast [this] value to [Observable].
 *
 * @return This value as an [Observable] or null.
 */
fun <T> T.tryGetObservable(): Observable<*>? = if (this is Observable<*>) this else null

fun <T> MutableIterator<T>.removeIf(predicate: (T) -> Boolean): Boolean {
    var removed = false
    this.forEach { element ->
        if (predicate(element)) {
            this.remove()
            if (!removed) {
                removed = true
            }
        }
    }
    return removed
}
