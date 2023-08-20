package de.thedead2.progression_reloaded.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.QuestWidget;
import de.thedead2.progression_reloaded.client.gui.themes.FuturisticTheme;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.data.quest.TestQuests;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static de.thedead2.progression_reloaded.client.gui.Alignment.CENTERED;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class ProgressionBookGUI extends Screen {
    //1 = Beginn Position in x Richtung, 2 = Beginn position in y Richtung, 3 = Breite des Auschnitts des Bildes, 4 = Höhe des Bildes
    //enableScissor(0, 0, this.width, this.height);

    //pX = x start position; pY = y start Position --> jeweils von oben links des screen
    //pUOffset = wiederholungspunkt des bildes in x richtung; pVOffset = wiederholungspunkt des bildes in y richtung
    //pUWidth = Ausschnittbreite des bildes, pVHeight = Ausschnitthöhe des bildes
    //pWidth = breite des bereichs in dem gerendert wird; pHeight = Höhe des Bereichs
    //texturewidth = breite auf die das bild gestreckt/ gestaucht werden soll; textureHeight = streckungs-/stauchungshöhe des bildes
    //blit(pPoseStack, 0, yStart, 0, 0, this.width *//*+ Math.negateExact(xStart)*//*, this.height + yStart, this.width, relativeHeight);

    private final Player player;
    //private final ProgressionTheme theme = new FuturisticTheme();
    private final ImageRenderInfo background = new ImageRenderInfo(
            1024,
            386,
            0,
            0,
            RelativePosition.AnchorPoint.A,
            CENTERED,
            Padding.NONE,
            true,
            new ResourceLocation(MOD_ID, "textures/gui/themes/futuristic/background.png")
    );
    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
    }

    @Override
    protected void init() {
        /*this.background.setWidth(this.width);
        this.background.setHeight(this.height);*/
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.background.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
        //new QuestWidget(this.theme, TestQuests.TEST1, screenRenderInfo).render(poseStack, mouseX, mouseY, partialTick);
        //RenderUtil.renderEntityInInventory(renderInfo.getScreenCenterX(), renderInfo.getScreenCenterY(), 27, renderInfo.getMouseX(), renderInfo.getMouseY(), Minecraft.getInstance().player);
    }

}
