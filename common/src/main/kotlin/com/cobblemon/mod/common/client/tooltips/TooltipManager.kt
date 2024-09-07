/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object TooltipManager {
    private val tooltipGenerators = mutableListOf<TooltipGenerator>()
    private val seeMoreInfo by lazy { lang("tooltip.see_more_info").yellow() }

    fun registerTooltipGenerator(generator: TooltipGenerator) {
        tooltipGenerators.add(generator)
    }

    fun generateTooltips(stack: ItemStack, lines: MutableList<Component>, hasShiftDown: Boolean) {
        val standardLines = tooltipGenerators.flatMap {
            val innerLines = mutableListOf<Component>()
            val regularTooltip = it.generateTooltip(stack, lines)
            if (regularTooltip?.isNotEmpty() == true) {
                innerLines.addAll(regularTooltip)
            }
            return@flatMap innerLines
        }
        val categoryLines = tooltipGenerators.flatMap {
            val innerLines = mutableListOf<Component>()
            val categoryTooltip = it.generateCategoryTooltip(stack, lines)
            if (categoryTooltip?.isNotEmpty() == true) {
                innerLines.addAll(categoryTooltip)
            }
            return@flatMap innerLines
        }
        val additionalLines = tooltipGenerators.flatMap {
            val innerLines = mutableListOf<Component>()
            val additionalTooltip = it.generateAdditionalTooltip(stack, lines)
            if (additionalTooltip?.isNotEmpty() == true) {
                innerLines.addAll(additionalTooltip)
            }
            return@flatMap innerLines
        }

        if (standardLines.isNotEmpty()) {
            lines.addAll(standardLines)
        }

        if (categoryLines.isNotEmpty()) {
            if (!(hasShiftDown && additionalLines.isNotEmpty())) {
                lines.addAll(categoryLines)
            }
        }

        if (additionalLines.isNotEmpty()) {
            if (hasShiftDown) {
                lines.addAll(additionalLines)
            } else {
                lines.add(seeMoreInfo)
            }
        }
    }
}