/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.X_AXIS
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation.Companion.Y_AXIS
import net.minecraft.client.model.geom.ModelPart

fun ModelPart.createTransformation() = ModelPartTransformation(this)

fun ModelPart.getPosition(axis: Int) = when (axis) {
    X_AXIS -> x
    Y_AXIS -> y
    else -> z
}
fun Bone.getRotation(axis: Int) = if (this is ModelPart) {
    when (axis) {
        X_AXIS -> xRot
        Y_AXIS -> yRot
        else -> zRot
    }
} else 0f

fun Bone.setRotation(axis: Int, angleInRadians: Float): Bone {
    if (this is ModelPart) {
        when (axis) {
            X_AXIS -> xRot = angleInRadians
            Y_AXIS -> yRot= angleInRadians
            else -> zRot = angleInRadians
        }
    }

    return this
}
fun ModelPart.setPosition(axis: Int, position: Float): ModelPart {
    when (axis) {
        X_AXIS -> x = position
        Y_AXIS -> y = position
        else -> z = position
    }
    return this
}
fun Bone.addRotation(axis: Int, differenceInRadians: Float) = setRotation(axis, getRotation(axis) + differenceInRadians)
fun ModelPart.addPosition(axis: Int, difference: Float) = setPosition(axis, getPosition(axis) + difference)
fun ModelPart.getChildOf(vararg path: String): ModelPart {
    var part = this
    for (piece in path) {
        part = part.getChild(piece)
    }
    return part
}
fun ModelPart.childNamed(vararg path: String): Pair<String, ModelPart> {
    var final = path.last()
    return final to getChildOf(*path)
}