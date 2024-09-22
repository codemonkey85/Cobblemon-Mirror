package com.cobblemon.mod.common.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public class ExclamationRenderType extends RenderType {

    private static final ResourceLocation EXCLAMATION_TEXTURE = ResourceLocation.fromNamespaceAndPath("test", "textures/particle/icon_exclamation.png");

    private static final ShaderStateShard POSITION_TEX_COLOR_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);

    private ExclamationRenderType(String s, VertexFormat v, VertexFormat.Mode m, Integer i, Boolean bl1, Boolean bl2, Runnable r1, Runnable r2) {   // irrelevant
        super(s, v, m, i, bl1, bl2, r1, r2);
    }

    public static final RenderType EXCLAMATION_RENDER_TYPE =
            create(
                "exclamation_icon",
                POSITION_TEX_COLOR,             // format
                VertexFormat.Mode.QUADS,        // mode
                1536,                           // buffer size
                false,                          // affectsCrumbling
                true,                           // sortOnUpload
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_TEX_COLOR_SHADER)
                        .setTextureState(new TextureStateShard(EXCLAMATION_TEXTURE, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
            );
}

