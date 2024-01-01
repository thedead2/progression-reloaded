package de.thedead2.progression_reloaded.client.gui.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.Math;
import java.text.DecimalFormat;
import java.util.Arrays;

import static net.minecraft.client.gui.GuiComponent.blit;


public class RenderUtil {

    private static final DecimalFormat DEBUG_FORMAT = new DecimalFormat("0.00 px");

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
            throw new IllegalArgumentException("Color parameter outside of expected range:" + badComponentString);
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
            entity = entityType.create(Minecraft.getInstance().level); //FIXME: returns sometimes null
        }
        renderEntityInternal(entity, xPos, yPos, scale, xRot, yRot);
    }


    private static void renderEntityInternal(LivingEntity entity, int xPos, int yPos, int scale, float xRot, float yRot) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate((float) xPos, (float) yPos, 1050.0F);
        modelViewStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.0F, 0.0F, 1000.0F);
        poseStack.scale((float) scale, (float) scale, (float) scale);
        flip(poseStack, Axis.ZP);

        entity.setYBodyRot(180 + yRot);
        entity.setYHeadRot(180 + yRot);
        entity.setXRot(xRot);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, buffer, 15728880));
        buffer.endBatch();
        entityrenderdispatcher.setRenderShadow(true);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }


    /**
     * Flips the given {@link PoseStack} 180 degrees around the given {@link Axis}.
     *
     * @param poseStack The {@link PoseStack} to flip.
     * @param axis      The {@link Axis} the {@link PoseStack} should be flipped around.
     *
     * @return The applied rotation in form of a {@link Quaternionf}
     **/
    @CanIgnoreReturnValue
    public static Quaternionf flip(PoseStack poseStack, Axis axis) {
        Quaternionf rotation = axis.rotation((float) Math.PI);
        poseStack.mulPose(rotation);
        return rotation;
    }


    /**
     * Rotates the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to rotate
     * @param rotation  the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param anchor    the rotation point in form of a {@link Vector3f}
     **/
    public static void rotateAround(PoseStack poseStack, Quaternionf rotation, Vector3f anchor) {
        rotateAround(poseStack, rotation, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Rotates the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to rotate
     * @param rotation  the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param pX        the x coordinate of the rotation point
     * @param pY        the y coordinate of the rotation point
     * @param pZ        the z coordinate of the rotation point
     **/
    public static void rotateAround(PoseStack poseStack, Quaternionf rotation, float pX, float pY, float pZ) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.rotateAround(rotation, pX, pY, pZ);
        poseStack.mulPoseMatrix(matrix4f);
    }


    /**
     * Rotates the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to rotate
     * @param rotation the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param anchor   the rotation point in form of a {@link Vector3f}
     **/
    public static void rotateAround(Matrix4f matrix4f, Quaternionf rotation, Vector3f anchor) {
        rotateAround(matrix4f, rotation, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Rotates the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to rotate
     * @param rotation the rotation in form of a {@link Quaternionf} to apply to the {@link PoseStack}
     * @param pX       the x coordinate of the rotation point
     * @param pY       the y coordinate of the rotation point
     * @param pZ       the z coordinate of the rotation point
     **/
    public static void rotateAround(Matrix4f matrix4f, Quaternionf rotation, float pX, float pY, float pZ) {
        matrix4f.rotateAround(rotation, pX, pY, pZ);
    }


    /**
     * Scales the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to scale
     * @param scale     the scaling factor in form of a {@link Vector3f}
     * @param anchor    the scaling point in form of a {@link Vector3f}
     **/
    public static void scaleAround(PoseStack poseStack, Vector3f scale, Vector3f anchor) {
        scaleAround(poseStack, scale.x, scale.y, scale.z, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Scales the given {@link PoseStack} around the given point
     *
     * @param poseStack the {@link PoseStack} to scale
     * @param scaleX    the scale factor of the x-axis
     * @param scaleY    the scale factor of the y-axis
     * @param scaleZ    the scale factor of the z-axis
     * @param pX        the x coordinate of the scaling point
     * @param pY        the y coordinate of the scaling point
     * @param pZ        the z coordinate of the scaling point
     **/
    public static void scaleAround(PoseStack poseStack, float scaleX, float scaleY, float scaleZ, float pX, float pY, float pZ) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.scaleAround(scaleX, scaleY, scaleZ, pX, pY, pZ);
        poseStack.mulPoseMatrix(matrix4f);
    }


    /**
     * Scales the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to scale
     * @param scale    the scaling factor in form of a {@link Vector3f}
     * @param anchor   the scaling point in form of a {@link Vector3f}
     **/
    public static void scaleAround(Matrix4f matrix4f, Vector3f scale, Vector3f anchor) {
        scaleAround(matrix4f, scale.x, scale.y, scale.z, anchor.x, anchor.y, anchor.z);
    }


    /**
     * Scales the given {@link Matrix4f} around the given point
     *
     * @param matrix4f the {@link Matrix4f} to scale
     * @param scaleX   the scale factor of the x-axis
     * @param scaleY   the scale factor of the y-axis
     * @param scaleZ   the scale factor of the z-axis
     * @param pX       the x coordinate of the scaling point
     * @param pY       the y coordinate of the scaling point
     * @param pZ       the z coordinate of the scaling point
     **/
    public static void scaleAround(Matrix4f matrix4f, float scaleX, float scaleY, float scaleZ, float pX, float pY, float pZ) {
        matrix4f.scaleAround(scaleX, scaleY, scaleZ, pX, pY, pZ);
    }


    /**
     * @param xRot the x rotation
     * @param yRot the y rotation
     * @param zRot the z rotation
     *
     * @return a {@link Quaternionf} that holds the given x-, y- and z-rotation
     **/
    public static Quaternionf rotateDegrees(float xRot, float yRot, float zRot) {
        return new Quaternionf().rotationX(xRot * ((float) Math.PI / 180F)).rotationY(yRot * ((float) Math.PI / 180F)).rotationZ(zRot * ((float) Math.PI / 180F));
    }


    public static Vector3f _getCenter(float xMin, float xMax, float yMin, float yMax, float zPos) {
        return getCenter(xMin, yMin, zPos, xMax - xMin, yMax - yMin);
    }

    public static Vector3f getCenter(float xPos, float yPos, float zPos, float objectWidth, float objectHeight) {
        float a = objectWidth / 2;
        float b = objectHeight / 2;
        return new Vector3f(xPos + a, yPos + b, zPos);
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
        else {
            font.draw(poseStack, text, startPos.x, startPos.y, color);
        }
    }


    public static void renderItem(ItemStack item, float xPos, float yPos, float zPos, float size) {
        renderItem(item, xPos, yPos, zPos, size, new Quaternionf());
    }


    public static void renderItem(ItemStack item, float xPos, float yPos, float zPos, float size, Quaternionf rotation) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.applyModelViewMatrix();
        itemRenderer.blitOffset = zPos;
        itemRenderer.renderAndDecorateItem(item, Math.round(xPos), Math.round(yPos));
        itemRenderer.blitOffset = 0.0F;

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        /*
        BakedModel bakedModel = itemRenderer.getModel(item, null, null, 0);
        minecraft.textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(xPos + (size / 2), yPos + (size / 2), zPos + itemRenderer.blitOffset);
        modelViewStack.translate(4.0F, 4.0F, 0.0F);
        modelViewStack.scale(1.0F, -1.0F, 1.0F);
        modelViewStack.scale(size, size, 1);
        modelViewStack.mulPose(rotation);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedModel.usesBlockLight();
        if(flag) {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(item, ItemTransforms.TransformType.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if(flag) {
            Lighting.setupFor3DItems();
        }

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();*/
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


    /**
     * Draws the given texture without transformation using a relative height to the given screenWidth and centering the texture
     **/
    public static void blitCenteredWithClipping(PoseStack poseStack, int screenWidth, int screenHeight, float ratio) {
        var relativeHeight = Math.round(screenWidth / ratio);
        int yStart = Math.negateExact((screenHeight - relativeHeight) / 2);
        blit(poseStack, 0, 0, 0, yStart, screenWidth, screenHeight, screenWidth, relativeHeight);
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


    public static Vector2d getMousePos() {
        Minecraft minecraft = Minecraft.getInstance();
        double mouseX = (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth());
        double mouseY = (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight());
        return new Vector2d(mouseX, mouseY);
    }


    public static void renderObjectOutline(PoseStack poseStack, ScreenComponent renderObject) {
        poseStack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        Area area = renderObject.getArea();

        renderArea(poseStack, area, Color.RED.getRGB(), Color.GREEN.getRGB());

        renderCross(poseStack, area.getCenterX(), area.getCenterY(), area.getZ(), 6, Color.ORANGE.getRGB());

        poseStack.popPose();
    }
    public static void renderObjectOutlineDebug(PoseStack poseStack, ScreenComponent renderObject) {
        poseStack.pushPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        Area area = renderObject.getArea();

        renderAreaDebug(poseStack, area, Color.RED.getRGB(), Color.GREEN.getRGB());

        renderCrossDebug(poseStack, area.getCenterX(), area.getCenterY(), area.getZ(), 6, Color.ORANGE.getRGB());

        poseStack.popPose();
    }


    public static void renderArea(PoseStack poseStack, Area area, int outerColor, int innerColor) {
        renderSquareOutline(poseStack, area.getX(), area.getXMax(), area.getY(), area.getYMax(), area.getZ(), outerColor);
        renderSquareOutline(poseStack, area.getInnerX(), area.getInnerXMax(), area.getInnerY(), area.getInnerYMax(), area.getZ(), innerColor);
    }


    public static void renderAreaDebug(PoseStack poseStack, Area area, int outerColor, int innerColor) {
        renderSquareOutlineDebug(poseStack, area.getX(), area.getXMax(), area.getY(), area.getYMax(), area.getZ(), outerColor);
        renderSquareOutlineDebug(poseStack, area.getInnerX(), area.getInnerXMax(), area.getInnerY(), area.getInnerYMax(), area.getZ(), innerColor);
    }


    public static void renderCross(PoseStack poseStack, float xPos, float yPos, float zPos, float width, int color) {
        horizontalLine(poseStack, xPos - width / 2, xPos + width / 2, yPos, zPos, 2, color);
        verticalLine(poseStack, xPos, yPos - width / 2, yPos + width / 2, zPos, 2, color);
    }


    public static void renderCrossDebug(PoseStack poseStack, float xPos, float yPos, float zPos, float width, int color) {
        horizontalLine(poseStack, xPos - width / 2, xPos + width / 2, yPos, zPos, 2, color);
        verticalLine(poseStack, xPos, yPos - width / 2, yPos + width / 2, zPos, 2, color);

        Font font = Minecraft.getInstance().font;
        String text = "x: " + DEBUG_FORMAT.format(xPos) + ", y: " + DEBUG_FORMAT.format(yPos);
        font.draw(poseStack, text, xPos - (float) font.width(text) / 2, yPos + 2, color);
    }


    public static void renderSquareOutlineDebug(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, int color) {
        horizontalLine(poseStack, xMin, xMax, yMin, zPos, 2, color);
        horizontalLine(poseStack, xMin, xMax, yMax, zPos, 2, color);
        verticalLine(poseStack, xMin, yMin, yMax, zPos, 2, color);
        verticalLine(poseStack, xMax, yMin, yMax, zPos, 2, color);
        float height = yMax - yMin;
        float width = xMax - xMin;
        Font font = Minecraft.getInstance().font;
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        font.draw(poseStack, DEBUG_FORMAT.format(width * scale), xMin + ((width / 2) - ((float) font.width(DEBUG_FORMAT.format(width * scale)) / 2)), yMin - font.lineHeight - 1, color);
        font.draw(poseStack, DEBUG_FORMAT.format(height * scale), xMax + 1, yMin + ((height / 2) - (float) (font.lineHeight) / 2), color);
    }


    public static void renderSquareOutline(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, int color) {
        horizontalLine(poseStack, xMin, xMax, yMin, zPos, 2, color);
        horizontalLine(poseStack, xMin, xMax, yMax, zPos, 2, color);
        verticalLine(poseStack, xMin, yMin, yMax, zPos, 2, color);
        verticalLine(poseStack, xMax, yMin, yMax, zPos, 2, color);
    }


    public static void border(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, float lineWidth, float radius, int color) {
        border(poseStack, xMin, xMax, yMin, yMax, zPos, lineWidth, radius, radius, radius, radius, color);
    }


    public static void border(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, float lineWidth, float radiusTopLeft, float radiusTopRight, float radiusBottomRight, float radiusBottomLeft, int color) {
        float height = yMax - yMin;
        float halfHeight = height / 2;
        float width = xMax - xMin;
        float halfWidth = width / 2;
        float diagonalRadius = (float) Math.sqrt((height * height) + (width * width)) / 2f;

        radiusTopLeft = Mth.clamp(radiusTopLeft, 0, diagonalRadius);
        radiusTopRight = Mth.clamp(radiusTopRight, 0, diagonalRadius);
        radiusBottomRight = Mth.clamp(radiusBottomRight, 0, diagonalRadius);
        radiusBottomLeft = Mth.clamp(radiusBottomLeft, 0, diagonalRadius);


        //Top left
        float scale = Mth.clamp(radiusTopLeft / diagonalRadius, 0, 1);
        float invertedScale = 1 - scale;

        horizontalLine(poseStack, (xMin + halfWidth) - (halfWidth * invertedScale), xMin + halfWidth, yMin, zPos, lineWidth, color);
        circularLine(poseStack, xMin + halfWidth * scale, yMin + halfHeight * scale, zPos, halfWidth * scale, halfHeight * scale, 0.75f, 0.25f, lineWidth, color);
        verticalLine(poseStack, xMin, (yMin + halfHeight) - ((halfHeight) * invertedScale), yMin + halfHeight, zPos, lineWidth, color);


        //Top right
        scale = Mth.clamp(radiusTopRight / diagonalRadius, 0, 1);
        invertedScale = 1 - scale;

        horizontalLine(poseStack, (xMin + halfWidth), (xMin + halfWidth) + (halfWidth * invertedScale), yMin, zPos, lineWidth, color);
        circularLine(poseStack, xMax - halfWidth * scale, yMin + halfHeight * scale, zPos, (halfWidth) * scale, (halfHeight) * scale, 0f, 0.25f, lineWidth, color);
        verticalLine(poseStack, xMax, (yMin + halfHeight) - ((halfHeight) * invertedScale), yMin + halfHeight, zPos, lineWidth, color);

        //Bottom right
        scale = Mth.clamp(radiusBottomRight / diagonalRadius, 0, 1);
        invertedScale = 1 - scale;

        horizontalLine(poseStack, (xMin + halfWidth), (xMin + halfWidth) + (halfWidth * invertedScale), yMax, zPos, lineWidth, color);
        circularLine(poseStack, xMax - halfWidth * scale, yMax - halfHeight * scale, zPos, (halfWidth) * scale, (halfHeight) * scale, 0.25f, 0.25f, lineWidth, color);
        verticalLine(poseStack, xMax, (yMin + halfHeight), (yMin + halfHeight) + (halfHeight * invertedScale), zPos, lineWidth, color);

        //Bottom left
        scale = Mth.clamp(radiusBottomLeft / diagonalRadius, 0, 1);
        invertedScale = 1 - scale;

        horizontalLine(poseStack, (xMin + halfWidth) - (halfWidth * invertedScale), xMin + halfWidth, yMax, zPos, lineWidth, color);
        circularLine(poseStack, xMin + halfWidth * scale, yMax - halfHeight * scale, zPos, (halfWidth) * scale, (halfHeight) * scale, 0.5f, 0.25f, lineWidth, color);
        verticalLine(poseStack, xMin, (yMin + halfHeight), (yMin + halfHeight) + (halfHeight * invertedScale), zPos, lineWidth, color);
    }

    public static void horizontalLine(PoseStack poseStack, float xMin, float xMax, float yPos, float zPos, float lineWidth, int color) {
        if(xMax < xMin) {
            float i = xMin;
            xMin = xMax;
            xMax = i;
        }

        fill(poseStack, xMin, xMax, yPos - lineWidth / 2, yPos + lineWidth / 2, zPos, color);
    }


    public static void verticalLine(PoseStack poseStack, float xPos, float yMin, float yMax, float zPos, float lineWidth, int color) {
        if(yMax < yMin) {
            float i = yMin;
            yMin = yMax;
            yMax = i;
        }

        fill(poseStack, xPos - lineWidth / 2, xPos + lineWidth / 2, yMin, yMax, zPos, color);
    }


    public static void circularLine(PoseStack poseStack, float xPos, float yPos, float zPos, float radius, float lineWidth, int color) {
        circularLine(poseStack, xPos, yPos, zPos, radius, 0, 1f, lineWidth, color);
    }


    public static void circularLine(PoseStack poseStack, float xPos, float yPos, float zPos, float radius, float startPercent, float percentFilled, float lineWidth, int color) {
        circularLine(poseStack, xPos, yPos, zPos, radius, radius, startPercent, percentFilled, lineWidth, color);
    }


    public static void circularLine(PoseStack poseStack, float xPos, float yPos, float zPos, float xRadius, float yRadius, float startPercent, float percentFilled, float lineWidth, int color) {
        lineWidth = Math.max(lineWidth, 0.01f);

        int alpha = (color >> 24 & 255);
        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color & 255);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        poseStack.pushPose();

        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        _fillCircleOutline(poseStack.last().pose(), bufferbuilder, xPos, yPos, zPos, xRadius - lineWidth / 2, yRadius - lineWidth / 2, xRadius + lineWidth / 2, yRadius + lineWidth / 2, Mth.clamp(startPercent, 0f, 1f), Mth.clamp(percentFilled, 0f, 1f), red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferbuilder.end());

        poseStack.popPose();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    private static void _fillCircleOutline(Matrix4f matrix, BufferBuilder bufferBuilder, float xPos, float yPos, float zPos, float innerXRadius, float innerYRadius, float outerXRadius, float outerYRadius, float startPercent, float percentFilled, int red, int green, int blue, int alpha) {
        _fillCircleOutlineGradient(matrix, bufferBuilder, xPos, yPos, zPos, innerXRadius, innerYRadius, outerXRadius, outerYRadius, startPercent, percentFilled, red, green, blue, alpha, red, green, blue, alpha);
    }


    private static void _fillCircleOutlineGradient(Matrix4f matrix, BufferBuilder bufferBuilder, float xPos, float yPos, float zPos, float innerXRadius, float innerYRadius, float outerXRadius, float outerYRadius, float startPercent, float percentFilled, int redA, int greenA, int blueA, int alphaA, int redB, int greenB, int blueB, int alphaB) {
        final int vertices = Mth.floor(percentFilled * 100);

        for(int i = vertices; i >= 0; --i) {
            float f = (float) (((startPercent * 100) + (percentFilled * 100) * (double) i / vertices) * (2 * Math.PI) / 100);
            float sin = (float) Math.sin(f);
            float cos = (float) Math.cos(f);
            float outerXOffset = sin * outerXRadius;
            float outerYOffset = cos * outerYRadius;
            float innerXOffset = sin * innerXRadius;
            float innerYOffset = cos * innerYRadius;

            bufferBuilder.vertex(matrix, xPos + innerXOffset, yPos - innerYOffset, zPos).color(redA, greenA, blueA, alphaA).endVertex();
            bufferBuilder.vertex(matrix, xPos + outerXOffset, yPos - outerYOffset, zPos).color(redB, greenB, blueB, alphaB).endVertex();
        }
    }


    public static void circularLine(PoseStack poseStack, float xPos, float yPos, float zPos, float xRadius, float yRadius, float lineWidth, int color) {
        circularLine(poseStack, xPos, yPos, zPos, xRadius, yRadius, 0, 1f, lineWidth, color);
    }


    public static void fill(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float z, int color) {
        if(xMin < xMax) {
            float i = xMin;
            xMin = xMax;
            xMax = i;
        }

        if(yMin < yMax) {
            float j = yMin;
            yMin = yMax;
            yMax = j;
        }

        int alpha = (color >> 24 & 255);
        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color & 255);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        _fillColor(poseStack.last().pose(), bufferbuilder, xMin, xMax, yMin, yMax, z, red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    public static void fillCircle(PoseStack poseStack, float xPos, float yPos, float zPos, float radius, int color) {
        fillCircle(poseStack, xPos, yPos, zPos, radius, 0, 1f, color);
    }


    public static void fillCircle(PoseStack poseStack, float xPos, float yPos, float zPos, float radius, float startPercent, float percentFilled, int color) {
        int alpha = (color >> 24 & 255);
        int red = (color >> 16 & 255);
        int green = (color >> 8 & 255);
        int blue = (color & 255);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        _fillCircle(poseStack.last().pose(), bufferbuilder, xPos, yPos, zPos, radius, Mth.clamp(startPercent, 0f, 1f), Mth.clamp(percentFilled, 0f, 1f), red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    private static void _fillCircle(Matrix4f matrix, BufferBuilder bufferBuilder, float xPos, float yPos, float zPos, float radius, float startPercent, float percentFilled, int red, int green, int blue, int alpha) {
        _fillCircleOutlineGradient(matrix, bufferBuilder, xPos, yPos, zPos, 0, 0, radius, radius, startPercent, percentFilled, red, green, blue, alpha, red, green, blue, alpha);
    }


    public static void fillCircle(PoseStack poseStack, float xPos, float yPos, float zPos, float radius, float percentFilled, int color) {
        fillCircle(poseStack, xPos, yPos, zPos, radius, 0, percentFilled, color);
    }


    public static void enableScissor(float xMin, float xMax, float yMin, float yMax) {
        Window window = Minecraft.getInstance().getWindow();
        int windowHeight = window.getHeight();
        double guiScale = window.getGuiScale();
        double x = xMin * guiScale;
        double y = windowHeight - yMin * guiScale;
        double width = (xMax - xMin) * guiScale;
        double height = (yMax - yMin) * guiScale;
        RenderSystem.enableScissor((int) x, (int) y, Math.max(0, (int) width), Math.max(0, (int) height));
    }


    public static void linearGradient(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, float degrees, GradientColor... colors) {
        poseStack.pushPose();
        GuiComponent.enableScissor(Math.round(xMin), Math.round(yMin), Math.round(xMax), Math.round(yMax));
        Arrays.sort(colors);
        float height = yMax - yMin;
        float width = xMax - xMin;

        float scale = scaleToFit(width, height, degrees);

        Vector3f center = getCenter(xMin, yMin, zPos, width, height);
        rotateAround(poseStack, Axis.ZP.rotationDegrees(degrees), center);
        scaleAround(poseStack, new Vector3f(scale, scale, 1), center);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillLinearGradient(poseStack.last().pose(), bufferbuilder, xMin, xMin + width, yMin, yMin + height, zPos, height, colors);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        RenderSystem.disableScissor();
        poseStack.popPose();
    }


    /**
     * @return a scale factor to scale an object with a given rotation to fit to the bounding box defined by the given width and height, so that there is no empty space within the bounding box
     */
    public static float scaleToFit(float width, float height, float degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        float W = (float) (width * cos + height * sin);
        float H = (float) (width * sin + height * cos);

        return Math.max(W / width, H / height);
    }


    private static void fillLinearGradient(Matrix4f matrix, BufferBuilder bufferBuilder, float xMin, float xMax, float yMin, float yMax, float zPos, float height, GradientColor... colors) {
        GradientColor first = colors[0];

        _fillColor(matrix, bufferBuilder, xMin, xMax, yMin, yMin + first.stopPercent() * height, zPos, first.red(), first.green(), first.blue(), first.alpha());

        for(int i = 0; i < colors.length; i++) {
            GradientColor colorA = colors[i];
            GradientColor colorB = (i + 1) < colors.length ? colors[i + 1] : null;
            float aYPos = yMin + colorA.stopPercent() * height;
            float bYPos = colorB == null ? yMax : yMin + colorB.stopPercent() * height;

            if(colorB != null) {
                _fillGradient(matrix, bufferBuilder, xMin, xMax, aYPos, bYPos, zPos, colorA.red(), colorA.green(), colorA.blue(), colorA.alpha(), colorB.red(), colorB.green(), colorB.blue(), colorB.alpha());
            }
        }

        GradientColor last = colors[colors.length - 1];

        _fillColor(matrix, bufferBuilder, xMin, xMax, yMin + last.stopPercent() * height, yMax, zPos, last.red(), last.green(), last.blue(), last.alpha());
    }


    private static void _fillColor(Matrix4f matrix, BufferBuilder bufferBuilder, float xMin, float xMax, float yMin, float yMax, float zPos, int red, int green, int blue, int alpha) {
        _fillGradient(matrix, bufferBuilder, xMin, xMax, yMin, yMax, zPos, red, green, blue, alpha, red, green, blue, alpha);
    }


    private static void _fillGradient(Matrix4f matrix, BufferBuilder bufferBuilder, float xMin, float xMax, float yMin, float yMax, float zPos, int redA, int greenA, int blueA, int alphaA, int redB, int greenB, int blueB, int alphaB) {
        bufferBuilder.vertex(matrix, xMax, yMin, zPos).color(redA, greenA, blueA, alphaA).endVertex();
        bufferBuilder.vertex(matrix, xMin, yMin, zPos).color(redA, greenA, blueA, alphaA).endVertex();
        bufferBuilder.vertex(matrix, xMin, yMax, zPos).color(redB, greenB, blueB, alphaB).endVertex();
        bufferBuilder.vertex(matrix, xMax, yMax, zPos).color(redB, greenB, blueB, alphaB).endVertex();
    }


    public static void radialGradient(PoseStack poseStack, float xMin, float xMax, float yMin, float yMax, float zPos, GradientColor... colors) {
        poseStack.pushPose();
        Arrays.sort(colors);
        GuiComponent.enableScissor(Math.round(xMin), Math.round(yMin), Math.round(xMax), Math.round(yMax));

        float height = yMax - yMin;
        float width = xMax - xMin;
        float diagonal = (float) Math.sqrt((height * height) + (width * width));

        Vector3f center = getCenter(xMin, yMin, zPos, width, height);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        fillRadialGradient(poseStack.last().pose(), bufferbuilder, center.x, center.y, zPos, diagonal / 2, colors);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.disableScissor();

        poseStack.popPose();
    }


    private static void fillRadialGradient(Matrix4f matrix, BufferBuilder bufferBuilder, float xPos, float yPos, float zPos, float radius, GradientColor... colors) {
        GradientColor first = colors[0];

        _fillCircleOutline(matrix, bufferBuilder, xPos, yPos, zPos, 0, 0, radius * first.stopPercent(), radius * first.stopPercent(), 0, 1f, first.red(), first.green(), first.blue(), first.alpha());

        for(int i = 0; i < colors.length; i++) {
            GradientColor colorA = colors[i];
            GradientColor colorB = (i + 1) < colors.length ? colors[i + 1] : null;

            float innerRadius = colorA.stopPercent() * radius;
            float outerRadius = colorB != null ? colorB.stopPercent() * radius : radius;

            if(colorB != null) {
                _fillCircleOutlineGradient(matrix, bufferBuilder, xPos, yPos, zPos, innerRadius, innerRadius, outerRadius, outerRadius, 0, 1f, colorA.red(), colorA.green(), colorA.blue(), colorA.alpha(), colorB.red(), colorB.green(), colorB.blue(), colorB.alpha());
            }
        }

        GradientColor last = colors[colors.length - 1];

        _fillCircleOutline(matrix, bufferBuilder, xPos, yPos, zPos, last.stopPercent() * radius, last.stopPercent() * radius, radius, radius, 0, 1f, last.red(), last.green(), last.blue(), last.alpha());
    }


    public static void radialGradient(PoseStack poseStack, float xPos, float yPos, float zPos, float radius, GradientColor... colors) {
        poseStack.pushPose();
        Arrays.sort(colors);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        fillRadialGradient(poseStack.last().pose(), bufferbuilder, xPos, yPos, zPos, radius, colors);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        poseStack.popPose();
    }


    public static void renderWithMask(PoseStack poseStack, int mouseX, int mouseY, float partialTick, Runnable mask, Runnable render) {
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        mask.run();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        render.run();
    }


    /**
     * @return a scale factor to scale an object with a given rotation to fit within the bounding box defined by the given width and height
     */
    public static float scaleToFitWithIn(float width, float height, float degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        float W = (float) (width * cos + height * sin);
        float H = (float) (width * sin + height * cos);

        return Math.min(width / W, height / H);
    }


    /**
     * @return a scale factor to scale an object with a given rotation to fit within the bounding box defined by the given width and height
     */
    public static float scaleToFitWithIn(float width, float height, double radians) {
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        float W = (float) (width * cos + height * sin);
        float H = (float) (width * sin + height * cos);

        return Math.min(width / W, height / H);
    }


    /**
     * @return a scale factor to scale an object with a given rotation to fit to the bounding box defined by the given width and height, so that there is no empty space within the bounding box
     */
    public static float scaleToFit(float width, float height, double radians) {
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        float W = (float) (width * cos + height * sin);
        float H = (float) (width * sin + height * cos);

        return Math.max(W / width, H / height);
    }


    public static boolean isWithin(float pX, float pY, float xPos, float yPos, float width, float height) {
        return pX >= xPos && pX <= xPos + width && pY >= yPos && pY <= yPos + height;
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
