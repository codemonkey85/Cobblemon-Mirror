package com.cobblemon.mod.common.client.render

/**
 * A [ClientPlayerIcon] that generalizes different icons as one.
 *
 * @author Segfault Guy
 * @since September 22nd, 2024
 */
class CompoundClientPlayerIcon(vararg icons: ClientPlayerIcon) : ClientPlayerIcon(){}