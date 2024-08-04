/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.entity.NPCSideDelegate
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.client.ClientMoLangFunctions
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.network.syncher.EntityDataAccessor

class NPCClientDelegate : PosableState(), NPCSideDelegate {
    lateinit var npcEntity: NPCEntity
    override val schedulingTracker
        get() = npcEntity.schedulingTracker
    override fun initialize(entity: NPCEntity) {
        this.npcEntity = entity
        this.age = entity.tickCount
    }

    override fun tick(entity: NPCEntity) {
        super.tick(entity)
        incrementAge(entity)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        super.onSyncedDataUpdated(data)
        if (data == NPCEntity.ASPECTS) {
            currentAspects = getEntity().entityData.get(NPCEntity.ASPECTS)
        }
    }

    override fun getEntity() = npcEntity

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
    }

    override fun addToStruct(struct: QueryStruct) {
        super.addToStruct(struct)
        struct.addFunctions(functions.functions)
        struct.addFunctions(ClientMoLangFunctions.clientFunctions)
        runtime.environment.query.addFunctions(struct.functions)
    }
}