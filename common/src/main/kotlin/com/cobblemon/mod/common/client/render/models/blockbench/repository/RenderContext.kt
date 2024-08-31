/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.util.asResource
import com.google.gson.reflect.TypeToken
import net.minecraft.world.entity.Entity
import net.minecraft.resources.ResourceLocation


/**
 * The RenderContext class manages a context for rendering operations by associating data values with specific keys.
 *
 * @author Waterpicker
 * @since 1.4.0
 */
class RenderContext {
    // A map to store data values associated with keys
    private val context: MutableMap<Key<*>, Any?> = mutableMapOf()

    var entity: Entity?
        get() = this.request(ENTITY)
        set(value) {
            if (value == null) {
                this.pop(ENTITY)
            } else {
                this.put(ENTITY, value)
            }
        }

    /**
     * Retrieves a value from the context associated with the provided key.
     *
     * @param key The key associated with the desired value.
     * @return The value associated with the key, or null if not found.
     *
     * @since 1.4.0
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> request(key: Key<T>): T? = this.context[key] as? T?

    /**
     * Retrieves a value from the context associated with the provided key, assuming the value exists.
     *
     * @param key The key associated with the desired value.
     * @return The value associated with the key.
     * @throws NullPointerException If the value is not present in the context.
     *
     * @since 1.4.0
     */
    fun <T : Any> requires(key: Key<T>): T = this.request(key) ?: throw NullPointerException("Required value not found in context for key: $key")

    /**
     * Stores a value in the context associated with the provided key.
     *
     * @param key   The key associated with the value.
     * @param value The value to be stored in the context.
     *
     * @since 1.4.0
     */
    fun <T : Any> put(key: Key<T>, value: T?) {
        this.context[key] = value
    }

    /**
     * Resets the context back to its initial state. In other words, this would represent an empty context set.
     *
     * @since 1.4.0
     */
    fun pop() = this.context.clear()

    fun <T : Any> pop(key: Key<T>) {
        this.context.remove(key)
    }

    /**
     * Represents a key used for accessing values in the context.
     *
     * @param key   The identifier associated with the key.
     * @param token The TypeToken representing the value's type.
     *
     * @since 1.4.0
     */
    data class Key<T : Any>(val key: ResourceLocation, val token: TypeToken<T>)

    /**
     * Represents different rendering states or modes.
     *
     * @since 1.4.0
     */
    enum class RenderState(
        /**
         * Indicates whether the mode is GUI-based.
         */
        val isGuiBased: Boolean
    ) {
        //World rendering mode.
        WORLD(false),

        //Portrait rendering mode (GUI-based).
        PORTRAIT(true),

        //Profile rendering mode (GUI-based).
        PROFILE(true),
        RESURRECTION_MACHINE(false),
        BLOCK(false)
    }

    // Predefined keys for common data types
    companion object {
        /**
         * Key to access the currently rendered entity.
         */
        val ENTITY: Key<Entity> = key("entity".asResource())

        /**
         * Key to access the identifier of the texture being rendered.
         */
        val TEXTURE: Key<ResourceLocation> = key("texture".asResource())

        /**
         * Key to access the base scaling factor of the current species.
         */
        val SCALE: Key<Float> = key("scale".asResource())

        /**
         * Key to access the identifier of the current species.
         */
        val SPECIES: Key<ResourceLocation> = key("species".asResource())

        /**
         * Key to access the aspects of the current entity.
         */
        val ASPECTS: Key<Set<String>> = key("species".asResource())

        /**
         * Key to access whether or not quirks are enabled for this context. It is implied as true when it's null
         */
        val DO_QUIRKS: Key<Boolean> = key("do_quirks".asResource())

        /**
         * Key to access the rendering state, indicating the active rendering mode.
         */
        val RENDER_STATE: Key<RenderState> = key("state".asResource())

        /**
         * Key to access the posable state of the thing being drawn.
         */
        val POSABLE_STATE: Key<PosableState> = key("posable_state".asResource())

        /**
         * Creates a new Key instance with the provided identifier and TypeToken.
         *
         * @param id    The identifier associated with the key.
         * @param token The TypeToken representing the value's type.
         * @return A new Key instance.
         *
         * @since 1.4.0
         */
        fun <T : Any> key(id: ResourceLocation, token: TypeToken<T>): Key<T> = Key(id, token)

        /**
         * Creates a new Key instance with the provided identifier and class type.
         *
         * @param id    The identifier associated with the key.
         * @param clazz The class type representing the value's type.
         * @return A new Key instance.
         *
         * @since 1.4.0
         */
        inline fun <reified T : Any> key(id: ResourceLocation): Key<T> = key(id, TypeToken.get(T::class.java))
    }
}

