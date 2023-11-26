package de.thedead2.progression_reloaded.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.client.gui.overlays.LevelProgressOverlay;
import de.thedead2.progression_reloaded.client.gui.overlays.QuestProgressOverlay;
import de.thedead2.progression_reloaded.client.gui.themes.ThemeManager;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Quaternionf;
import org.joml.Vector2d;

import java.awt.*;


public class ModRenderer {

    private final Minecraft minecraft;

    private final ThemeManager themeManager;

    private final NotificationToastRenderer toastRenderer;


    private LevelProgressOverlay levelProgressOverlay;

    private QuestProgressOverlay questProgressOverlay;


    private ItemStack extraLifeItem;

    private float animationStartTime = 0;

    private float animationTime = 1;

    private float randomActivationOffsetX = 0;

    private float randomActivationOffsetY = 0;

    private static boolean guiDebug = false;

    ModRenderer() {
        this.minecraft = Minecraft.getInstance();
        this.themeManager = new ThemeManager();
        this.toastRenderer = new NotificationToastRenderer();
    }


    public static boolean guiDebug() {
        guiDebug = !guiDebug;
        return guiDebug;
    }

    public static boolean isGuiDebug() {
        return guiDebug;
    }

    @SubscribeEvent
    public void onRender(final RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        int mouseX = (int) (minecraft.mouseHandler.xpos() * (double) event.getWindow().getGuiScaledWidth() / (double) event.getWindow().getScreenWidth());
        int mouseY = (int) (minecraft.mouseHandler.ypos() * (double) event.getWindow().getGuiScaledHeight() / (double) event.getWindow().getScreenHeight());
        render(event.getPoseStack(), mouseX, mouseY, event.getPartialTick());
    }


    @SubscribeEvent
    public void onPostScreenRender(final ScreenEvent.Render.Post event) {
        if(isGuiDebug() && this.minecraft.screen != null) {
            Vector2d mousePos = RenderUtil.getMousePos();
            RenderUtil.renderCrossDebug(new PoseStack(), (float) mousePos.x, (float) mousePos.y, 1000000, 5, Color.YELLOW.getRGB());
        }
    }


    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderProgressOverlayIfNeeded(poseStack, mouseX, mouseY, partialTick);
        this.toastRenderer.renderToastsIfNeeded(poseStack, mouseX, mouseY, partialTick);
        this.renderExtraLifeAnimationIfNeeded(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), partialTick);
    }


    private void renderProgressOverlayIfNeeded(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(ConfigManager.SHOULD_RENDER_OVERLAY.get() && this.minecraft.screen == null && !this.minecraft.player.isCreative() && !this.minecraft.player.isSpectator()) {
            if(this.levelProgressOverlay != null) {
                this.levelProgressOverlay.render(poseStack, mouseX, mouseY, partialTick);
            }
            if(this.questProgressOverlay != null) {
                this.questProgressOverlay.render(poseStack, mouseX, mouseY, partialTick);
            }
        }
    }


    private void renderExtraLifeAnimationIfNeeded(int guiWidth, int guiHeight, float partialTick) {
        if(animationStartTime > 0) {
            float timeLeft = animationTime - animationStartTime;
            float percentDone = (timeLeft + partialTick) / animationTime;
            float quadraticEasing = percentDone * percentDone;
            float cubicEasing = percentDone * quadraticEasing;
            float catMullRom = 10.25F * cubicEasing * quadraticEasing - 24.95F * quadraticEasing * quadraticEasing + 25.5F * cubicEasing - 13.8F * quadraticEasing + 4.0F * percentDone;
            float f4 = catMullRom * (float) Math.PI;
            float xStartPos = randomActivationOffsetX * (float) (guiWidth / 4);
            float yStartPos = randomActivationOffsetY * (float) (guiHeight / 4);


            float animationCurve = Mth.sin(f4);

            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            PoseStack poseStack = new PoseStack();

            poseStack.pushPose();

            float xTrans = (float) (guiWidth / 2) + xStartPos * Mth.abs(Mth.sin(f4 * 2.0F));
            float yTrans = (float) (guiHeight / 2) + yStartPos * Mth.abs(Mth.sin(f4 * 2.0F));
            float zTrans = -50.0F;

            poseStack.translate(xTrans, yTrans, zTrans);

            float scale = 50.0F + 175.0F * animationCurve;
            poseStack.scale(scale, -scale, scale);

            Quaternionf xRot = Axis.XP.rotationDegrees(6.0F * Mth.cos(percentDone * 8.0F));
            Quaternionf yRot = Axis.YP.rotationDegrees(900.0F * Mth.abs(animationCurve));
            Quaternionf zRot = Axis.ZP.rotationDegrees(6.0F * Mth.cos(percentDone * 8.0F));

            poseStack.mulPose(yRot);
            poseStack.mulPose(xRot);
            poseStack.mulPose(zRot);

            MultiBufferSource.BufferSource multibuffersource$buffersource = minecraft.renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().renderStatic(extraLifeItem, ItemTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, poseStack, multibuffersource$buffersource, 0);

            poseStack.popPose();
            multibuffersource$buffersource.endBatch();

            float yRotDegrees = 900.0F * Mth.abs(animationCurve) + 180;
            Quaternionf textYRot = Axis.YP.rotationDegrees(yRotDegrees);

            if(MathHelper.isRotationBetween(yRotDegrees, 277.5f, 87.5f)) {
                poseStack.translate(xTrans, yTrans, yTrans + 25);

                float textScale = 1.75f * animationCurve;
                poseStack.scale(textScale, textScale, 1);

                poseStack.mulPose(textYRot);
                poseStack.mulPose(xRot);
                poseStack.mulPose(zRot);

                GuiComponent.drawCenteredString(poseStack, minecraft.font, extraLifeItem.getHoverName(), 0, -(16 / 2), Color.WHITE.getRGB());
                poseStack.popPose();
            }

            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
            animationStartTime--;
        }
    }


    public void setLevelProgressOverlay(LevelProgressOverlay levelProgressOverlay) {
        this.levelProgressOverlay = levelProgressOverlay;
    }


    public void setQuestProgressOverlay(QuestProgressOverlay levelProgressOverlay) {
        this.questProgressOverlay = levelProgressOverlay;
    }


    public void displayExtraLifeAnimation() {
        extraLifeItem = new ItemStack(ModItems.EXTRA_LIFE.get());
        animationTime = MathHelper.secondsToTicks(15);
        animationStartTime = animationTime;
        RandomSource random = RandomSource.create();
        randomActivationOffsetX = random.nextFloat() * 2.0F - 1.0F;
        randomActivationOffsetY = random.nextFloat() * 2.0F - 1.0F;
    }


    public ThemeManager getThemeManager() {
        return themeManager;
    }


    public NotificationToastRenderer getToastRenderer() {
        return toastRenderer;
    }


    public void displayNewLevelInfoScreen(ResourceLocation levelId) {
        //this.minecraft.setScreen(new LevelInformationScreen(levelId));
    }


    public void updateLevelProgressOverlay(IProgressInfo<ProgressionLevel> progressInfo) {
        if(this.levelProgressOverlay != null) {
            this.levelProgressOverlay.updateProgress(progressInfo);
        }
    }


    public void updateQuestProgressOverlay(IProgressInfo<ProgressionQuest> progressInfo) {
        if(this.levelProgressOverlay != null) {
            this.questProgressOverlay.updateProgress(progressInfo);
        }
    }
}
