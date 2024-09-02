/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution

import com.cobblemon.mod.common.api.pokemon.evolution.*
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.controller.ClientEvolutionController
import com.cobblemon.mod.common.pokemon.evolution.controller.ServerEvolutionController

class CobblemonEvolutionProxy(
    private val pokemon: Pokemon,
) : EvolutionProxy<EvolutionDisplay, Evolution, ClientEvolutionController.Intermediate, ServerEvolutionController.Intermediate> {

    private var clientController = ClientEvolutionController(this.pokemon, emptySet())
    private var serverController = ServerEvolutionController(this.pokemon, emptySet(), emptySet())

    override fun isClient(): Boolean = this.pokemon.isClient

    override fun current(): EvolutionController<out EvolutionLike, *> = if (this.isClient()) this.clientController else this.serverController

    override fun client(): EvolutionController<EvolutionDisplay, ClientEvolutionController.Intermediate> {
        if (!this.isClient()) {
            throw ClassCastException("Cannot use the client implementation from the server side")
        }
        return this.clientController
    }

    override fun server(): EvolutionController<Evolution, ServerEvolutionController.Intermediate> {
        if (this.isClient()) {
            throw ClassCastException("Cannot use the server implementation from the client side")
        }
        return this.serverController
    }

    internal fun overrideController(newInstance: EvolutionController<out EvolutionLike, PreProcessor>) {
        when (newInstance) {
            is ClientEvolutionController -> {
                this.clientController = newInstance
            }
            is ServerEvolutionController -> {
                this.serverController = newInstance
            }
            else -> {
                throw IllegalArgumentException("Cannot resolve override of type ${newInstance::class.simpleName}")
            }
        }
    }

}