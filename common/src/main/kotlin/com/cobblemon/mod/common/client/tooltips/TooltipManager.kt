package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object TooltipManager {
    private val tooltipGenerators = mutableListOf<TooltipGenerator>()
    private val seeMoreInfo by lazy { lang("see_more_info").yellow() }

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

        val additionalLines = tooltipGenerators.flatMap {
            val innerLines = mutableListOf<Component>()
            val additionalTooltip = it.generateAdditionalTooltip(stack, lines)
            if (additionalTooltip?.isNotEmpty() == true) {
                if (hasShiftDown) {
                    innerLines.addAll(additionalTooltip)
                } else {
                    innerLines.add(seeMoreInfo)
                }
            }
            return@flatMap innerLines
        }

        if (standardLines.isNotEmpty()) {
            lines.addAll(standardLines)
        }

        if (additionalLines.isNotEmpty()) {
            lines.addAll(additionalLines)
        }
    }
}