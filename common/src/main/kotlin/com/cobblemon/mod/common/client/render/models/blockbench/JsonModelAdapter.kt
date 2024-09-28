/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.google.gson.InstanceCreator
import java.lang.reflect.Type

/**
 * An instance creator for a [PosableModel] that allows for the model to be constructed from a JSON object. This
 * is used to maintain a reference to the model so that other type adapters can call back to it.
 *
 * @author Hiroku
 * @since October 18th, 2022
 */
class JsonModelAdapter<T : PosableModel>(private val constructor: (Bone) -> T) : InstanceCreator<T> {
    companion object {
        var modelPart: Bone? = null
        var model: PosableModel? = null
    }
    override fun createInstance(type: Type): T {
        val rootBone = modelPart!!
        return constructor(rootBone).also {
            model = it
            it.registerPartAndAllNamedChildren("__root", rootBone)
        }
    }
}