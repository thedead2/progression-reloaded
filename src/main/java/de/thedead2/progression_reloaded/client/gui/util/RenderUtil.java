package de.thedead2.progression_reloaded.client.gui.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.client.gui.util.objects.RenderObject;
import de.thedead2.progression_reloaded.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.Comparator;

import static net.minecraft.client.gui.GuiComponent.blit;


public class RenderUtil {

    public static void rotate(int degrees) {

    }


    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, float pMouseX, float pMouseY, LivingEntity pLivingEntity) {
        float f = (float) Math.atan(pMouseX / 40.0F);
        float f1 = (float) Math.atan(pMouseY / 40.0F);
        renderEntityInInventoryRaw(pPosX, pPosY, pScale, f, f1, pLivingEntity);
    }


    public static void renderEntityInInventoryRaw(int pPosX, int pPosY, int pScale, float angleXComponent, float angleYComponent, LivingEntity pLivingEntity) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((float) pPosX, (float) pPosY, 1050.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0F, 0.0F, 1000.0F);
        posestack1.scale((float) pScale, (float) pScale, (float) pScale);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(angleYComponent * 20.0F * ((float) Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        posestack1.mulPose(quaternionf);
        float f2 = pLivingEntity.yBodyRot;
        float f3 = pLivingEntity.getYRot();
        float f4 = pLivingEntity.getXRot();
        float f5 = pLivingEntity.yHeadRotO;
        float f6 = pLivingEntity.yHeadRot;
        pLivingEntity.yBodyRot = 180.0F + angleXComponent * 20.0F;
        pLivingEntity.setYRot(180.0F + angleXComponent * 40.0F);
        pLivingEntity.setXRot(-angleYComponent * 20.0F);
        pLivingEntity.yHeadRot = pLivingEntity.getYRot();
        pLivingEntity.yHeadRotO = pLivingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternionf1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(pLivingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880));
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        pLivingEntity.yBodyRot = f2;
        pLivingEntity.setYRot(f3);
        pLivingEntity.setXRot(f4);
        pLivingEntity.yHeadRotO = f5;
        pLivingEntity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
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


    public static Vector2i getScreenCenter() {
        return new Vector2i(getScreenWidth() / 2, getScreenHeight() / 2);
    }


    public static int getScreenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }


    public static int getScreenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }


    public static void renderPerLayer(Collection<RenderObject> renderObjects, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderObjects.stream()
                     .sorted(Comparator.comparingInt(RenderObject::getRenderLayer))
                     .forEachOrdered(object -> object.render(poseStack, mouseX, mouseY, partialTicks));
    }


    public static Vector2i getScreenA() {
        return new Vector2i(0, 0);
    }


    public static Vector2i getScreenB() {
        return new Vector2i(getScreenWidth(), 0);
    }


    public static Vector2i getScreenC() {
        return new Vector2i(0, getScreenHeight());
    }


    public static Vector2i getScreenD() {
        return new Vector2i(getScreenWidth(), getScreenHeight());
    }


    private static final ItemStack extraLifeItem = new ItemStack(ModItems.EXTRA_LIFE.get());

    private static int itemActivationTicks = 0;

    private static float itemActivationOffX = 0;

    private static float itemActivationOffY = 0;


    //TODO: Animation is twice as fast as intended --> why?
    public static void renderExtraLifeAnimation(int guiWidth, int guiHeight, float partialTick) {
        if(itemActivationTicks > 0) {
            int i = 40 - itemActivationTicks;
            float f = ((float) i + partialTick) / 40.0F;
            float f1 = f * f;
            float f2 = f * f1;
            float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
            float f4 = f3 * (float) Math.PI;
            float f5 = itemActivationOffX * (float) (guiWidth / 4);
            float f6 = itemActivationOffY * (float) (guiHeight / 4);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            PoseStack posestack = new PoseStack();
            posestack.pushPose();
            posestack.translate((float) (guiWidth / 2) + f5 * Mth.abs(Mth.sin(f4 * 2.0F)), (float) (guiHeight / 2) + f6 * Mth.abs(Mth.sin(f4 * 2.0F)), -50.0F);
            float f7 = 50.0F + 175.0F * Mth.sin(f4);
            posestack.scale(f7, -f7, f7);
            posestack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(f4))));
            posestack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            posestack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().renderStatic(extraLifeItem, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, posestack, multibuffersource$buffersource, 0);
            /*PoseStack poseStack2 = new PoseStack();
            poseStack2.pushPose();
            poseStack2.scale(2, 2, 2);*/
            GuiComponent.drawCenteredString(new PoseStack(), Minecraft.getInstance().font, extraLifeItem.getHoverName(), guiWidth / 2, guiHeight / 2, 16777215);
            //poseStack2.popPose();
            posestack.popPose();
            multibuffersource$buffersource.endBatch();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
            itemActivationTicks--;
        }
    }


    public static void displayExtraLifeAnimation() {
        itemActivationTicks = 40;
        RandomSource random = RandomSource.create();
        itemActivationOffX = random.nextFloat() * 2.0F - 1.0F;
        itemActivationOffY = random.nextFloat() * 2.0F - 1.0F;
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
