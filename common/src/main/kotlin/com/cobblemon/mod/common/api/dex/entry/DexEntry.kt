package com.cobblemon.mod.common.api.dex.entry

import net.minecraft.resources.ResourceLocation

class DexEntry(
    val registryId: ResourceLocation,
    val entryId: ResourceLocation,
    val extraData: List<ExtraDexData>
) {

}