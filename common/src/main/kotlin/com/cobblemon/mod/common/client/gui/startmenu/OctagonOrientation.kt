package com.cobblemon.mod.common.client.gui.startmenu

import net.minecraft.world.phys.Vec2

enum class OctagonOrientation(val vec2: Vec2, val angle: Int) {
    TOP(Vec2(0F, -1F).normalized(), 2),
    LEFT(Vec2(-1F, 0F).normalized(), 0),
    RIGHT(Vec2(1F, 0F).normalized(),4),
    BOTTOM(Vec2(0F, 1F).normalized(),6),
    TOP_LEFT(Vec2(-1F, -1F).normalized(), 1),
    TOP_RIGHT(Vec2(1F, -1F).normalized(), 3),
    BOTTOM_LEFT(Vec2(-1F, 1F).normalized(),7),
    BOTTOM_RIGHT(Vec2(1F, 1F).normalized(),5),
}