package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.animation.*;
import de.thedead2.progression_reloaded.client.gui.components.QuestWidget;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.themes.ThemeManager;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.data.quest.TestQuests;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;


public class ProgressionBookGUI extends Screen {

    private final IAnimation animation = new SimpleAnimation(0, MathHelper.secondsToTicks(10), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN, InterpolationTypes.LINEAR);

    private final IAnimation strikeThrough = new SimpleAnimation(0, MathHelper.secondsToTicks(5), LoopTypes.LOOP, AnimationTypes.EASE_OUT, InterpolationTypes.BOUNCE);
    private DrawableTexture background;
    private TextBox textBox;

    private QuestWidget questWidget;
    private ProgressionFont font;
    private final Player player;

    private RenderableAnimation typeWriterAnimation;


    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.background.draw(poseStack);
        this.questWidget.render(poseStack, mouseX, mouseY, partialTick);
        //this.typeWriterAnimation.render(poseStack, mouseX, mouseY, partialTick);
    }


    /*@Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        this.textBox.keyPressed(pKeyCode, pScanCode, pModifiers);
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }


    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.textBox.charTyped(codePoint, modifiers);
    }*/


    /*@Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return this.textBox.mouseClicked(pMouseX, pMouseY, pButton);
    }


    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return this.textBox.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }


    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return this.textBox.mouseReleased(pMouseX, pMouseY, pButton);
    }


    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return this.textBox.mouseScrolled(pMouseX, pMouseY, pDelta);
    }*/


    @Override
    protected void init() {
        ThemeManager themeManager = ModClientInstance.getInstance().getModRenderer().getThemeManager();
        this.background = new DrawableTexture(themeManager.getActiveTheme().get().backgroundFrame(), new Area(0, 0, 0, this.width, this.height, new Padding(25)));
        this.questWidget = new QuestWidget(new Area(50, 50, 0, 50, 50), themeManager.getActiveTheme().get().questWidgetHovered(), TestQuests.TEST1.getDisplay(), themeManager.getActiveTheme().get().tooltip());
        this.animation.loop(LoopTypes.LOOP).reset();
    }
}
