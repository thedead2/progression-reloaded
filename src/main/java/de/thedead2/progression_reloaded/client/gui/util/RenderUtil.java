package de.thedead2.progression_reloaded.client.gui.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.*;

import java.awt.*;
import java.lang.Math;

import static net.minecraft.client.gui.GuiComponent.blit;
import static net.minecraft.client.gui.GuiComponent.fill;


public class RenderUtil {

    private static LivingEntity entity = null;


    public static Color colorFromHex(String hex) {
        return Color.decode(hex);
    }


    public static int getColorFromHex(String hex) {
        return Integer.decode(hex);
    }


    public static int getColor(float red, float green, float blue) {
        return Mth.color(red, green, blue);
    }

    private static void testColorValueRange(int red, int green, int blue, int alpha) {
        boolean rangeError = false;
        String badComponentString = "";

        if ( alpha < 0 || alpha > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Alpha";
        }
        if ( red < 0 || red > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Red";
        }
        if ( green < 0 || green > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Green";
        }
        if ( blue < 0 || blue > 255) {
            rangeError = true;
            badComponentString = badComponentString + " Blue";
        }
        if (rangeError) {
            throw new IllegalArgumentException("Color parameter outside of expected range:"
                                                       + badComponentString);
        }
    }

    public static void storeColorComponents(int[] colorHolder, int color) {
        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color & 255);
        int alpha = (color >> 24 & 255);
        testColorValueRange(red, green, blue, alpha);

        colorHolder[0] = red;
        colorHolder[1] = green;
        colorHolder[2] = blue;
        colorHolder[3] = alpha;
    }

    public static int convertColor(int[] colorHolder) {
        return ((colorHolder[3] & 255) << 24) |
                ((colorHolder[0] & 255) << 16) |
                ((colorHolder[1] & 255) << 8)  |
                ((colorHolder[2] & 255));
    }


    @SuppressWarnings("DataFlowIssue")
    public static void renderEntity(EntityType<? extends LivingEntity> entityType, int xPos, int yPos, int scale, float xRot, float yRot) {
        if(entity == null || !entity.getType().equals(entityType)) {
            entity = entityType.create(Minecraft.getInstance().level); //TODO: returns sometimes null
        }
        renderEntityInternal(entity, xPos, yPos, scale, xRot, yRot);
    }


    private static void renderEntityInternal(LivingEntity entity, int xPos, int yPos, int scale, float xRot, float yRot) {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate((float) xPos, (float) yPos, 1050.0F);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack poseStack1 = new PoseStack();
        poseStack1.translate(0.0F, 0.0F, 1000.0F);
        poseStack1.scale((float) scale, (float) scale, (float) scale);
        flip(poseStack1, Axis.ZP);

        entity.setYBodyRot(180 + yRot);
        entity.setYHeadRot(180 + yRot);
        entity.setXRot(xRot);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack1, multibuffersource$buffersource, 15728880));
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }


    /**
     * Flips the pose stack 180 degrees around the given axis.
     *
     * @param poseStack The pose stack to flip.
     * @param axis      The axis the pose stack should be flipped around.
     *
     * @return The applied rotation in form of a Quaternionf
     **/
    @CanIgnoreReturnValue
    public static Quaternionf flip(PoseStack poseStack, Axis axis) {
        Quaternionf rotation = axis.rotation((float) Math.PI);
        poseStack.mulPose(rotation);
        return rotation;
    }

    public static int changeAlpha(int color, float alphaPercent) {
        return Math.max(Math.round(alphaPercent * 255), 4) << 24 | color & 0xFFFFFF; //TODO: Find out why the alpha of the color stays by one when under 4???
    }

    public static Vector2f centerObject(float xPos, float yPos, float areaWidth, float areaHeight, float objectWidth, float objectHeight) {
        float xStart = xPos + (areaWidth / 2 - objectWidth / 2);
        float yStart = yPos + (areaHeight / 2 - objectHeight / 2);

        return new Vector2f(xStart, yStart);
    }

    public static void drawCenteredText(PoseStack poseStack, Component text, Font font, float xMin, float yMin, float width, float height, boolean withShadow, int color) {
        Vector2f startPos = centerObject(xMin, yMin, width, height, font.width(text), font.lineHeight);
        if(withShadow) {
            font.drawShadow(poseStack, text, startPos.x, startPos.y, color);
        }
        else font.draw(poseStack, text, startPos.x, startPos.y, color);
    }

    public static void renderItem(PoseStack poseStack, float xPos, float yPos, float scale, ItemStack icon) {
        renderItem(poseStack, xPos, yPos, scale, 0, 0, icon);
    }


    public static void renderItem(PoseStack poseStack, float xPos, float yPos, float scale, float xRot, float yRot, ItemStack item) {
        poseStack.pushPose();

        poseStack.translate(xPos, yPos, 0);
        poseStack.scale(scale, -scale, 1);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Minecraft.getInstance().getItemRenderer().renderStatic(item, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, poseStack, multibuffersource$buffersource, 0);
        poseStack.popPose();
        multibuffersource$buffersource.endBatch();
    }


    public static void blitCenteredWithClipping(PoseStack poseStack, int screenWidth, int screenHeight, float textureWidth, float textureHeight, ResourceLocation texture) {
        blitCenteredWithClipping(poseStack, screenWidth, screenHeight, textureWidth / textureHeight, texture);
    }


    /**
     * Draws the given texture without transformation using a relative width to the given screenHeight and centering the texture
     **/
    public static void blitCenteredWithClipping(PoseStack poseStack, int screenWidth, int screenHeight, float ratio, ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
        var relativeWidth = Math.round(screenHeight * ratio);
        int xStart = (screenWidth - relativeWidth) / 2;
        blit(poseStack, xStart, 0, 0, 0, screenWidth + Math.negateExact(xStart), screenHeight, relativeWidth, screenHeight);
    }


    public static void drawTexture(ResourceLocation texture, Matrix4f transformationMatrix, Vector2f a, Vector2f d, float z, Vector2f textureA, Vector2f textureD) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(transformationMatrix, a.x, d.y, z).uv(textureA.x, textureD.y).endVertex();
        bufferbuilder.vertex(transformationMatrix, d.x, d.y, z).uv(textureD.x, textureD.y).endVertex();
        bufferbuilder.vertex(transformationMatrix, d.x, a.y, z).uv(textureD.x, textureA.x).endVertex();
        bufferbuilder.vertex(transformationMatrix, a.x, a.y, z).uv(textureA.x, textureA.x).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
    }


    public static Vector3f getScreenCenter() {
        return new Vector3f(getScreenWidth() / 2, getScreenHeight() / 2, 0);
    }


    public static float getScreenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }


    public static float getScreenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }


    public static Vector2i getScreenA() {
        return new Vector2i(0, 0);
    }


    public static Vector2i getScreenB() {
        return new Vector2i(Math.round(getScreenWidth()), 0);
    }


    public static Vector2i getScreenC() {
        return new Vector2i(0, Math.round(getScreenHeight()));
    }


    public static Vector2i getScreenD() {
        return new Vector2i(Math.round(getScreenWidth()), Math.round(getScreenHeight()));
    }


    public static void renderObjectOutline(PoseStack poseStack, ScreenComponent renderObject) {
        poseStack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        Area area = renderObject.getArea();

        renderArea(poseStack, area, Color.RED.getRGB(), Color.GREEN.getRGB());

        renderCross(poseStack, area.getCenterX(), area.getCenterY(), 6, Color.ORANGE.getRGB());

        poseStack.popPose();
    }
    public static void renderObjectOutlineDebug(PoseStack poseStack, ScreenComponent renderObject) {
        poseStack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        Area area = renderObject.getArea();

        renderAreaDebug(poseStack, area, Color.RED.getRGB(), Color.GREEN.getRGB());

        renderCross(poseStack, area.getCenterX(), area.getCenterY(), 6, Color.ORANGE.getRGB());

        poseStack.popPose();
    }


    public static void renderArea(PoseStack poseStack, Area area, int outerColor, int innerColor) {
        renderSquareOutline(poseStack, area.getX(), area.getXMax(), area.getY(), area.getYMax(), outerColor);
        renderSquareOutline(poseStack, area.getInnerX(), area.getInnerXMax(), area.getInnerY(), area.getInnerYMax(), innerColor);
    }
    public static void renderAreaDebug(PoseStack poseStack, Area area, int outerColor, int innerColor) {
        renderSquareOutlineDebug(poseStack, area.getX(), area.getXMax(), area.getY(), area.getYMax(), outerColor);
        renderSquareOutlineDebug(poseStack, area.getInnerX(), area.getInnerXMax(), area.getInnerY(), area.getInnerYMax(), innerColor);
    }


    public static void renderCross(PoseStack poseStack, float xPos, float yPos, float width, int color) {
        horizontalLine(poseStack, xPos - width / 2, xPos + width / 2, yPos, 2, color);
        verticalLine(poseStack, xPos, yPos - width / 2, yPos + width / 2, 2, color);
    }


    public static void renderSquareOutlineDebug(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, int color) {
        horizontalLine(poseStack, xMin, xMax, yMin, 2, color);
        horizontalLine(poseStack, xMin, xMax, yMax, 2, color);
        verticalLine(poseStack, xMin, yMin, yMax, 2, color);
        verticalLine(poseStack, xMax, yMin, yMax, 2, color);
        float height = yMax - yMin;
        float width = xMax - xMin;
        Font font = Minecraft.getInstance().font;
        font.draw(poseStack, width + " px", xMin + ((width / 2) - ((float) font.width(width + " px") / 2)), yMin - font.lineHeight - 1, color);
        font.draw(poseStack, height + " px", xMax + 1, yMin + ((height / 2) - (float) (font.lineHeight) / 2), color);
    }
    public static void renderSquareOutline(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, int color) {
        horizontalLine(poseStack, xMin, xMax, yMin, 2, color);
        horizontalLine(poseStack, xMin, xMax, yMax, 2, color);
        verticalLine(poseStack, xMin, yMin, yMax, 2, color);
        verticalLine(poseStack, xMax, yMin, yMax, 2, color);
    }


    public static void horizontalLine(PoseStack poseStack, float xMin, float xMax, float yPos, float lineWidth, int color) {
        if(xMax < xMin) {
            float i = xMin;
            xMin = xMax;
            xMax = i;
        }

        fill(poseStack, Math.round(xMin), Math.round(yPos), Math.round(xMax + lineWidth / 2), Math.round(yPos + lineWidth / 2), color);
    }


    public static void verticalLine(PoseStack poseStack, float xPos, float yMin, float yMax, float lineWidth, int color) {
        if(yMax < yMin) {
            float i = yMin;
            yMin = yMax;
            yMax = i;
        }

        fill(poseStack, Math.round(xPos), Math.round(yMin + lineWidth / 2), Math.round(xPos + lineWidth / 2), Math.round(yMax), color);
    }
    /*public static void render9Sprite(PoseStack pPoseStack, int pX, int pY, int pWidth, int pHeight, int pPadding, int pUWidth, int pVHeight, int pUOffset, int pVOffset) {
        this.blit(pPoseStack, pX, pY, pUOffset, pVOffset, pPadding, pPadding);
        renderRepeating(pPoseStack, pX + pPadding, pY, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset, pUWidth - pPadding - pPadding, pVHeight);
        this.blit(pPoseStack, pX + pWidth - pPadding, pY, pUOffset + pUWidth - pPadding, pVOffset, pPadding, pPadding);
        this.blit(pPoseStack, pX, pY + pHeight - pPadding, pUOffset, pVOffset + pVHeight - pPadding, pPadding, pPadding);
        renderRepeating(pPoseStack, pX + pPadding, pY + pHeight - pPadding, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset + pVHeight - pPadding, pUWidth - pPadding - pPadding, pVHeight);
        this.blit(pPoseStack, pX + pWidth - pPadding, pY + pHeight - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pVHeight - pPadding, pPadding, pPadding);
        renderRepeating(pPoseStack, pX, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
        renderRepeating(pPoseStack, pX + pPadding, pY + pPadding, pWidth - pPadding - pPadding, pHeight - pPadding - pPadding, pUOffset + pPadding, pVOffset + pPadding, pUWidth - pPadding - pPadding, pVHeight - pPadding - pPadding);
        renderRepeating(pPoseStack, pX + pWidth - pPadding, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
    }

    private static void renderRepeating(PoseStack pPoseStack, int pX, int pY, int pBorderToU, int pBorderToV, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
        for(int i = 0; i < pBorderToU; i += pUWidth) {
            int j = pX + i;
            int k = Math.min(pUWidth, pBorderToU - i);

            for(int l = 0; l < pBorderToV; l += pVHeight) {
                int i1 = pY + l;
                int j1 = Math.min(pVHeight, pBorderToV - l);
                this.blit(pPoseStack, j, i1, pUOffset, pVOffset, k, j1);
            }
        }

    }*/
}
