package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.themes.ThemeManager;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;


public class ProgressionBookGUI extends Screen {

    private final IAnimation animation = new SimpleAnimation(0, MathHelper.secondsToTicks(10), LoopTypes.NO_LOOP, AnimationTypes.EASE_IN_OUT, InterpolationTypes.CATMULL_ROM(0, 1.5f, 2, 1));

    private DrawableTexture background;
    private final Player player;


    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.animation.animate(0, 100, f -> this.background.getArea().setX(f));

        this.background.draw(poseStack);
    }


    @Override
    protected void init() {
        ThemeManager themeManager = ModClientInstance.getInstance().getModRenderer().getThemeManager();
        this.background = new DrawableTexture(themeManager.getActiveTheme().get().backgroundFrame(), new Area(0, 0, 0, this.width, this.height, new Padding(25)));
        this.animation.reset();
    }
}
