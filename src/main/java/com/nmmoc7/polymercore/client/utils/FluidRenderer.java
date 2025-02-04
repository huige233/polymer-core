/*
 * Modified form com.simibubi.create.foundation.render.FluidRenderer
 * Credits:
 * MIT License
 * <p>
 * Copyright (c) 2019 simibubi
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.nmmoc7.polymercore.client.utils;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import com.nmmoc7.polymercore.client.renderer.CustomRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;


@OnlyIn(Dist.CLIENT)
public class FluidRenderer {

    public static IVertexBuilder getFluidBuilder(IRenderTypeBuffer buffer) {
        return buffer.getBuffer(CustomRenderTypes.FLUID);
    }

    public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
                                         boolean inbound, IRenderTypeBuffer buffer, MatrixStack ms, int light) {
        renderFluidStream(fluidStack.getFluid(), direction, radius, progress, inbound, getFluidBuilder(buffer), ms, light);
    }

    public static void renderFluidStream(FluidState fluidState, Direction direction, float radius, float progress,
                                         boolean inbound, IRenderTypeBuffer buffer, MatrixStack ms, int light) {
        renderFluidStream(fluidState.getFluid(), direction, radius, progress, inbound, getFluidBuilder(buffer), ms, light);
    }

    public static void renderFluidStream(Fluid fluid, Direction direction, float radius, float progress,
                                         boolean inbound, IVertexBuilder builder, MatrixStack ms, int light) {
        FluidAttributes fluidAttributes = fluid.getAttributes();
        Function<ResourceLocation, TextureAtlasSprite> spriteAtlas = Minecraft.getInstance()
            .getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite flowTexture = spriteAtlas.apply(fluidAttributes.getFlowingTexture());
        TextureAtlasSprite stillTexture = spriteAtlas.apply(fluidAttributes.getStillTexture());

        int color = fluidAttributes.getColor();
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity());
        light = (light & 0xF00000) | luminosity << 4;

        if (inbound)
            direction = direction.getOpposite();


        ms.push();
        ms.translate(0.5, 0.5, 0.5);
        ms.rotate(Vector3f.YP.rotationDegrees(direction.getHorizontalAngle()));
        ms.rotate(Vector3f.XP.rotationDegrees(direction == Direction.UP ? 0 : direction == Direction.DOWN ? 180 : 90));
        ms.translate(-0.5, -0.5, -0.5);

        float h = (float) (radius);
        float hMin = (float) (-radius);
        float hMax = (float) (radius);
        float y = inbound ? 0 : .5f;
        float yMin = y;
        float yMax = y + MathHelper.clamp(progress * .5f - 1e-6f, 0, 1);

        for (int i = 0; i < 4; i++) {
            ms.push();
            renderTiledHorizontalFace(h, Direction.SOUTH, hMin, yMin, hMax, yMax, builder, ms, light, color,
                flowTexture);
            ms.pop();
            ms.rotate(Vector3f.YP.rotationDegrees(90));
        }

        if (progress != 1)
            renderTiledVerticalFace(yMax, Direction.UP, hMin, hMin, hMax, hMax, builder, ms, light, color,
                stillTexture);

        ms.pop();
    }

    public static void renderTiledFluidBB(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax,
                                          float yMax, float zMax, IRenderTypeBuffer buffer, MatrixStack ms, int light, boolean renderBottom) {
        renderTiledFluidBB(fluidStack.getFluid(), xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom);
    }

    public static void renderTiledFluidBB(FluidState fluidState, float xMin, float yMin, float zMin, float xMax,
                                          float yMax, float zMax, IRenderTypeBuffer buffer, MatrixStack ms, int light, boolean renderBottom) {
        renderTiledFluidBB(fluidState.getFluid(), xMin, yMin, zMin, xMax, yMax, zMax, getFluidBuilder(buffer), ms, light, renderBottom);
    }

    public static void renderTiledFluidBB(Fluid fluid, float xMin, float yMin, float zMin, float xMax,
                                          float yMax, float zMax, IVertexBuilder builder, MatrixStack ms, int light, boolean renderBottom) {

        FluidAttributes fluidAttributes = fluid.getAttributes();
        TextureAtlasSprite fluidTexture = Minecraft.getInstance()
            .getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
            .apply(fluidAttributes.getStillTexture());

        int color = fluidAttributes.getColor();
        int blockLightIn = (light >> 4) & 0xF;
        int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity());
        light = (light & 0xF00000) | luminosity << 4;

        Vector3d center = new Vector3d(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);

        ms.push();
        if (fluid.getAttributes()
            .isLighterThanAir()) {

            ms.translate(center.x, center.y, center.z);
            ms.rotate(Vector3f.XP.rotationDegrees(180));
            ms.translate(-center.x, -center.y, -center.z);
        }


        for (Direction side : Direction.values()) {
            if (side == Direction.DOWN && !renderBottom)
                continue;

            if (side.getAxis()
                .isHorizontal()) {
                ms.push();

                if (side.getAxisDirection() == AxisDirection.NEGATIVE) {
                    ms.translate(center.x, center.y, center.z);
                    ms.rotate(Vector3f.YP.rotationDegrees(180));
                    ms.translate(-center.x, -center.y, -center.z);
                }

                boolean X = side.getAxis() == Axis.X;
                int darkColor = Color.mixColors(color, 0xff000011, 1 / 4f);
                renderTiledHorizontalFace(X ? xMax : zMax, side, X ? zMin : xMin, yMin, X ? zMax : xMax, yMax, builder,
                    ms, light, darkColor, fluidTexture);

                ms.pop();
                continue;
            }

            renderTiledVerticalFace(side == Direction.UP ? yMax : yMin, side, xMin, zMin, xMax, zMax, builder, ms,
                light, color, fluidTexture);
        }

        ms.pop();

    }

    private static void renderTiledVerticalFace(float y, Direction face, float xMin, float zMin, float xMax, float zMax,
                                                IVertexBuilder builder, MatrixStack ms, int light, int color, TextureAtlasSprite texture) {
        float x2 = 0;
        float z2 = 0;
        for (float x1 = xMin; x1 < xMax; x1 = x2) {
            x2 = Math.min((int) (x1 + 1), xMax);
            for (float z1 = zMin; z1 < zMax; z1 = z2) {
                z2 = Math.min((int) (z1 + 1), zMax);

                float u1 = texture.getInterpolatedU(local(x1) * 16);
                float v1 = texture.getInterpolatedV(local(z1) * 16);
                float u2 = texture.getInterpolatedU(x2 == xMax ? local(x2) * 16 : 16);
                float v2 = texture.getInterpolatedV(z2 == zMax ? local(z2) * 16 : 16);

                putVertex(builder, ms, x1, y, z2, color, u1, v2, face, light);
                putVertex(builder, ms, x2, y, z2, color, u2, v2, face, light);
                putVertex(builder, ms, x2, y, z1, color, u2, v1, face, light);
                putVertex(builder, ms, x1, y, z1, color, u1, v1, face, light);
            }
        }
    }

    private static void renderTiledHorizontalFace(float h, Direction face, float hMin, float yMin, float hMax,
                                                  float yMax, IVertexBuilder builder, MatrixStack ms, int light, int color, TextureAtlasSprite texture) {
        boolean X = face.getAxis() == Axis.X;

        float h2 = 0;
        float y2 = 0;

        for (float h1 = hMin; h1 < hMax; h1 = h2) {
            h2 = Math.min((int) (h1 + 1), hMax);
            for (float y1 = yMin; y1 < yMax; y1 = y2) {
                y2 = Math.min((int) (y1 + 1), yMax);

                int multiplier = texture.getWidth() == 32 ? 8 : 16;
                float u1 = texture.getInterpolatedU(local(h1) * multiplier);
                float v1 = texture.getInterpolatedV(local(y1) * multiplier);
                float u2 = texture.getInterpolatedU(h2 == hMax ? local(h2) * multiplier : multiplier);
                float v2 = texture.getInterpolatedV(y2 == yMax ? local(y2) * multiplier : multiplier);

                float x1 = X ? h : h1;
                float x2 = X ? h : h2;
                float z1 = X ? h1 : h;
                float z2 = X ? h2 : h;

                putVertex(builder, ms, x2, y2, z1, color, u1, v2, face, light);
                putVertex(builder, ms, x1, y2, z2, color, u2, v2, face, light);
                putVertex(builder, ms, x1, y1, z2, color, u2, v1, face, light);
                putVertex(builder, ms, x2, y1, z1, color, u1, v1, face, light);
            }
        }
    }

    private static float local(float f) {
        if (f < 0)
            f += 10;
        return f - ((int) f);
    }

    private static void putVertex(IVertexBuilder builder, MatrixStack ms, float x, float y, float z, int color, float u,
                                  float v, Direction face, int light) {

        Vector3i n = face.getDirectionVec();
        Entry peek = ms.getLast();
        int ff = 0xff;
        int a = color >> 24 & ff;
        int r = color >> 16 & ff;
        int g = color >> 8 & ff;
        int b = color & ff;

        builder.pos(peek.getMatrix(), x, y, z)
            .color(r, g, b, a)
            .tex(u, v)
            .overlay(OverlayTexture.NO_OVERLAY)
            .lightmap(light)
            .normal(n.getX(), n.getY(), n.getZ())
            .endVertex();
    }

}
