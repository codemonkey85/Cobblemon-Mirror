package com.cobblemon.mod.common.client.gui.startmenu

import net.minecraft.resources.ResourceLocation

data class OctagonGUIOption(
    val name: String,
    val iconResource: ResourceLocation,
    val onPress: () -> Unit
)