package de.thedead2.progression_reloaded.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import de.thedead2.progression_reloaded.client.gui.overlays.LevelOverlay;
import de.thedead2.progression_reloaded.client.gui.overlays.QuestOverlay;
import de.thedead2.progression_reloaded.client.gui.themes.ThemeManager;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.items.ModItems;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;


public class ModRenderer {

    private static final Path CLIENT_DATA = ModHelper.DIR_PATH.resolve("client.dat");
    private final Minecraft minecraft;

    private final ThemeManager themeManager;

    private final ToastRenderer toastRenderer;

    @Nullable
    private LevelOverlay levelOverlay;

    @Nullable
    private QuestOverlay questOverlay;


    private ItemStack extraLifeItem;

    private float animationStartTime = 0;

    private float animationTime = 1;

    private float randomActivationOffsetX = 0;

    private float randomActivationOffsetY = 0;

    private static boolean guiDebug = false;

    ModRenderer() {
        this.minecraft = Minecraft.getInstance();
        this.themeManager = new ThemeManager();
        this.toastRenderer = new ToastRenderer();
    }


    public static boolean guiDebug() {
        guiDebug = !guiDebug;
        return guiDebug;
    }

    public static boolean isGuiDebug() {
        return guiDebug;
    }


    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderProgressOverlayIfNeeded(poseStack, mouseX, mouseY, partialTick);
        this.toastRenderer.renderToastsIfNeeded(poseStack, mouseX, mouseY, partialTick);
        this.renderExtraLifeAnimationIfNeeded(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), partialTick);
    }


    private void renderProgressOverlayIfNeeded(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(ConfigManager.SHOULD_RENDER_OVERLAY.get() && this.minecraft.screen == null && !this.minecraft.player.isCreative() && !this.minecraft.player.isSpectator()) {
            if(this.levelOverlay != null) {
                this.levelOverlay.render(poseStack, mouseX, mouseY, partialTick);
            }
            if(this.questOverlay != null) {
                this.questOverlay.render(poseStack, mouseX, mouseY, partialTick);
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


    public ToastRenderer getToastRenderer() {
        return toastRenderer;
    }


    public boolean isQuestFollowed(ResourceLocation questId) {
        return this.questOverlay != null && this.questOverlay.isQuestFollowed(questId);
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


    public void pinLastFollowedQuest() {
        try {
            CompoundTag tag = NbtIo.readCompressed(CLIENT_DATA.toFile());
            ResourceLocation questId = SerializationHelper.getNullable(tag, "lastFollowedQuest", tag1 -> new ResourceLocation(tag1.getAsString()));

            this.updateQuestProgressOverlay(ModClientInstance.getInstance().getClientData().getQuestData().getOrStartProgress(questId));
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to read client data!", e, Level.ERROR);
        }
    }


    public void displayNewLevelInfoScreen(ResourceLocation levelId) {
        //this.minecraft.setScreen(new LevelInformationScreen(levelId));
    }


    public void updateQuestProgressOverlay(IProgressInfo<ProgressionQuest> progressInfo) {
        if(this.questOverlay != null) {
            this.questOverlay.updateProgress(progressInfo);
        }
        else {
            this.setQuestProgressOverlay(GuiFactory.createQuestOverlay(progressInfo.getProgressable().getDisplay(), (QuestProgress) progressInfo));
        }

        this.updateLevelProgressOverlay(ModClientInstance.getInstance().getClientData().getCurrentLevelProgress());
    }


    public void setQuestProgressOverlay(@Nullable QuestOverlay levelProgressOverlay) {
        this.questOverlay = levelProgressOverlay;
    }


    //TODO: create LevelOverlay and QuestOverlay when logging into new world and save which quest was pinned when logging out + re-pin this quest when logging in!
    public void updateLevelProgressOverlay(IProgressInfo<ProgressionLevel> progressInfo) {
        if(this.levelOverlay != null) {
            this.levelOverlay.updateProgress(progressInfo);
        }
        else {
            this.setLevelProgressOverlay(GuiFactory.createLevelOverlay(progressInfo.getProgressable().getDisplay(), (LevelProgress) progressInfo));
        }
    }


    public void setLevelProgressOverlay(@Nullable LevelOverlay levelOverlay) {
        this.levelOverlay = levelOverlay;
    }


    public void saveLastFollowedQuest() {
        try {
            CompoundTag tag = new CompoundTag();
            SerializationHelper.addNullable(this.questOverlay, tag, "lastFollowedQuest", questOverlay -> StringTag.valueOf(questOverlay.getFollowedQuest().toString()));
            NbtIo.writeCompressed(tag, CLIENT_DATA.toFile());
        }
        catch(IOException e) {
            CrashHandler.getInstance().handleException("Failed to save client data to file!", e, Level.ERROR);
        }
    }
}
