package com.cobblemon.mod.common.server.net.pokedex

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokedex.MapUpdatePacket
import io.netty.util.internal.PlatformDependent.putByte
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.saveddata.maps.MapId
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object MapUpdatePacketHandler : ServerNetworkPacketHandler<MapUpdatePacket> {

    override fun handle(packet: MapUpdatePacket, server: MinecraftServer, player: ServerPlayer) {
        player.server.execute {
            (player.level() as? ServerLevel)?.let { serverLevel ->
                updatePlayerMap(player, packet.imageBytes, serverLevel)
            }
        }
    }

    private fun updatePlayerMap(player: ServerPlayer, imageBytes: ByteArray, world: ServerLevel) {
        val pictureEdgeTrim = 8
    //    println("Received image bytes: ${imageBytes.size}")
        val image = ImageIO.read(imageBytes.inputStream())
        if (image == null) {
            player.displayClientMessage(Component.literal("Failed to read image from bytes"), false)
            return
        }
    //    println("Image dimensions: ${image.width}x${image.height}")

        // Save the received image to file for debugging
        val receivedImageFile = File("received_image.png")
        ImageIO.write(convertToBufferedImage(image), "png", receivedImageFile)
    //    println("Saved received image to ${receivedImageFile.absolutePath}")

        // Resize the image to 128x128
        val resizedImage = convertToBufferedImage(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH))

        // Save the resized image to file for debugging
        val resizedImageFile = File("resized_image.png")
        ImageIO.write(resizedImage, "png", resizedImageFile)
    //    println("Saved resized image to ${resizedImageFile.absolutePath}")

        // Convert the resized image to a pixel array and trim the edges
        val pixels = trimPixelArray(convertPixelArray(resizedImage), pictureEdgeTrim)
        val mapColors = expandMapColors(MapColor.MATERIAL_COLORS.filterNotNull().toTypedArray())

        val inventory = player.inventory
        for (i in 0 until inventory.containerSize) {
            val stack = inventory.getItem(i)
            if (stack.item == Items.MAP) {
                val mapStack = ItemStack(Items.FILLED_MAP)
                val mapId = world.freeMapId.id
                val serializationContext = world.registryAccess().createSerializationContext(NbtOps.INSTANCE)
                val dimensionNbt = serializationContext.withEncoder(DimensionType.CODEC).apply(world.dimensionTypeRegistration()).result().get()

                val nbt = CompoundTag().apply {
                    put("dimension", dimensionNbt)
                    putInt("xCenter", 0)
                    putInt("zCenter", 0)
                    putBoolean("locked", true)
                    putBoolean("unlimitedTracking", false)
                    putBoolean("trackingPosition", false)
                    putByte("scale", 3.toByte())
                    put("banners", ListTag())
                }
                val mapState = MapItemSavedData.load(nbt, world.registryAccess())

                for (x in 0 until 128 - 2 * pictureEdgeTrim) {
                    for (y in 0 until 128 - 2 * pictureEdgeTrim) {
                        val color = Color(pixels[y][x], true)
                        val nearestColor = nearestColor(mapColors, color)
                        mapState.colors[x + pictureEdgeTrim + (y + pictureEdgeTrim) * 128] = nearestColor.toByte()
    //                    println("Processed pixel at ($x, $y): original color = (${color.red}, ${color.green}, ${color.blue}), nearest map color index = $nearestColor")
                    }
                }

                world.setMapData(MapId(mapId), mapState)
                val mapIdComponent = MapId(mapId)
                mapStack.set(DataComponents.MAP_ID, mapIdComponent)

                inventory.setItem(i, mapStack)
                player.displayClientMessage(Component.literal("SnapPicture: Map updated with screenshot"), true)
    //            println("Map updated successfully with mapId: $mapId")
                return
            }
        }
        player.displayClientMessage(Component.literal("No empty map found in inventory"), true)
        println("No empty map found in inventory")
    }

    private fun trimPixelArray(pixels: Array<IntArray>, trim: Int): Array<IntArray> {
        val trimmedWidth = pixels[0].size - 2 * trim
        val trimmedHeight = pixels.size - 2 * trim
        val trimmedPixels = Array(trimmedHeight) { IntArray(trimmedWidth) }

        for (y in 0 until trimmedHeight) {
            for (x in 0 until trimmedWidth) {
                trimmedPixels[y][x] = pixels[y + trim][x + trim]
            }
        }

        return trimmedPixels
    }

    private fun convertToBufferedImage(img: Image): BufferedImage {
        return if (img is BufferedImage) {
            img
        } else {
            val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
            val bGr = bimage.createGraphics()
            bGr.drawImage(img, 0, 0, null)
            bGr.dispose()
            bimage
        }
    }

    private fun convertPixelArray(image: BufferedImage): Array<IntArray> {
        val width = image.width
        val height = image.height
        return Array(height) { y ->
            IntArray(width) { x -> image.getRGB(x, y) }
        }
    }

    private fun expandMapColors(mapColors: Array<MapColor>): List<Color> {
        val expandedColors = mutableListOf<Color>()
        for (color in mapColors) {
            val baseColor = Color(color.calculateRGBColor(MapColor.Brightness.NORMAL))
            for (coeff in shadeCoeffs) {
                expandedColors.add(Color((baseColor.red * coeff).toInt(), (baseColor.green * coeff).toInt(), (baseColor.blue * coeff).toInt()))
            }
        }
        return expandedColors
    }

    private fun nearestColor(mapColors: List<Color>, color: Color): Int {
        var closestColorIndex = 0
        var minDistance = Double.MAX_VALUE

        /*// Check if the color is close to black
        if (color.red < 20 && color.green < 20 && color.blue < 20) {
            return mapColors.indexOfFirst { it.red < 20 && it.green < 20 && it.blue < 20 && it.alpha != 0 }
        }*/

        for (i in mapColors.indices) {
            val mcColor = mapColors[i]

            // Skip transparent colors
            //if (mcColor.alpha == 0) continue

            val distance = colorDistance(color.red, color.green, color.blue, mcColor.red, mcColor.green, mcColor.blue)
            if (distance < minDistance) {
                minDistance = distance
                closestColorIndex = i
            }
        }

        // If the chosen color is transparent, set it to black
        if (mapColors[closestColorIndex].alpha == 0) {
            closestColorIndex = mapColors.indexOfFirst { it.red == 0 && it.green == 0 && it.blue == 0 }
        }

        //println("Mapped color (${color.red}, ${color.green}, ${color.blue}) to index $closestColorIndex")

        // ensure that color index 0 (transparent) does not get used for black
        if (closestColorIndex == 0){
            return 207 // either 207 or 119
        }

        return closestColorIndex
    }

    private fun colorDistance(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return Math.sqrt(dr * dr + dg * dg + db * db)
    }

    private val shadeCoeffs = doubleArrayOf(180.0 / 255.0, 220.0 / 255.0, 255.0 / 255.0, 135.0 / 255.0)
}
