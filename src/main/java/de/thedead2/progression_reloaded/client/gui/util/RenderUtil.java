package de.thedead2.progression_reloaded.client.gui.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.client.gui.util.objects.RenderObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.awt.*;

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


    public static void renderItem(PoseStack poseStack, float xPos, float yPos, float scale, ItemStack icon) {
        renderItem(poseStack, xPos, yPos, scale, 0, 0, icon);
    }


    public static void renderItem(PoseStack poseStack, float xPos, float yPos, float scale, float xRot, float yRot, ItemStack item) {
        poseStack.pushPose();

        poseStack.translate(xPos, yPos, 0);
        poseStack.scale(scale, scale, 1);
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


    public static void renderObjectOutline(PoseStack poseStack, RenderObject renderObject) {
        poseStack.pushPose();
        renderObject.getPoseStackTransformation(poseStack);

        renderArea(poseStack, renderObject.getObjectArea(), Color.MAGENTA.getRGB());
        renderArea(poseStack, renderObject.getRenderArea(), Color.ORANGE.getRGB());

        renderCross(poseStack, 0, 0, 8, Color.RED.getRGB());

        poseStack.popPose();
    }


    public static void renderArea(PoseStack poseStack, Area area, int color) {
        renderSquareOutline(poseStack, area.getXMin(), area.getXMax(), area.getYMin(), area.getYMax(), color);
    }


    public static void renderCross(PoseStack poseStack, float xPos, float yPos, float width, int color) {
        horizontalLine(poseStack, xPos - width / 2, xPos + width / 2, yPos, 2, color);
        verticalLine(poseStack, xPos, yPos - width / 2, yPos + width / 2, 2, color);
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
