package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedText;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.themes.ThemeManager;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;


public class ProgressionBookGUI extends Screen {

    private final IAnimation animation = new SimpleAnimation(0, MathHelper.secondsToTicks(30), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC);
    private final IAnimation strikeThrough = new SimpleAnimation(0, MathHelper.secondsToTicks(5), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC);
    private final IAnimation fadeOut = new SimpleAnimation(0, MathHelper.secondsToTicks(5), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC);
    private final IAnimation fadeOut2 = new SimpleAnimation(0, MathHelper.secondsToTicks(5), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CUBIC);
    private DrawableTexture background;
    private TextBox textBox;
    private ProgressionFont font;
    private final Player player;


    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
    }



    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.background.draw(poseStack);
        //this.animation.animate(300, 0, t -> this.textBox.getArea().setWidth(t));

        this.textBox.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    protected void init() {
        ThemeManager themeManager = ModClientInstance.getInstance().getModRenderer().getThemeManager();
        this.background = new DrawableTexture(themeManager.getActiveTheme().get().backgroundFrame(), new Area(0, 0, 0, this.width, this.height, new Padding(25)));
        this.textBox = new TextBox(new Area(50, 50, 0, 300, 100, new Padding(5)), List.of(
                new FormattedText("Just Testing!", new ResourceLocation(ModHelper.MOD_ID, "expansiva"), FontFormatting.defaultFormatting().setTextAlignment(Alignment.CENTERED).setLineHeight(11), false)
        ), Color.GRAY.getRGB());
        this.font = FontManager.getFont(new ResourceLocation(ModHelper.MOD_ID, "expansiva")).setLineHeight(50);
        this.animation.reset();
    }
}
