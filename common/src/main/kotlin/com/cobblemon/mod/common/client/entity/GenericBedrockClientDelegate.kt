/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.entity

import com.cobblemon.mod.common.api.entity.EntitySideDelegate
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity

class GenericBedrockClientDelegate : EntitySideDelegate<GenericBedrockEntity>, PosableState() {
    lateinit var currentEntity: GenericBedrockEntity
    override val schedulingTracker
        get() = getEntity().schedulingTracker
    override fun getEntity() = currentEntity

    override fun initialize(entity: GenericBedrockEntity) {
        super.initialize(entity)
        this.currentEntity = entity
        this.age = entity.tickCount
        this.currentModel = VaryingModelRepository.getPoser(entity.category, entity.delegate as GenericBedrockClientDelegate)
        currentModel!!.updateLocators(entity, this)
        updateLocatorPosition(entity.position())

        val currentPoseType = entity.getCurrentPoseType()
        // Doing this awful thing because otherwise evolution particle won't start until the client looks at it. Which sucks slightly more than this.
        val pose = this.currentModel!!.getFirstSuitablePose(this, currentPoseType)
        doLater { setPose(pose.poseName) }
    }

    override fun tick(entity: GenericBedrockEntity) {
        super.tick(entity)
        updateLocatorPosition(entity.position())
        incrementAge(entity)
    }

    override fun updatePartialTicks(partialTicks: Float) {
        this.currentPartialTicks = partialTicks
    }
}