package de.thedead2.progression_reloaded.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.util.ConfigManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;

public class ProgressionBookGUI extends Screen {
    /* This combination
            0,
            100,
            50,
            new Area.Position(60, 45, Area.Point.A),
            new Padding(10, 5),
            new ResourceLocation(MOD_ID, "textures/gui/themes/futuristic/background.png"),
            false,
            (float) 3072/1157,
            ImageRenderObject.FixedParameter.WIDTH,
            Alignment.CENTERED

            is with repetition --> why?
     */

    private final Player player;
    private final ProgressionTheme theme;
    private final ImageRenderObject background = new ImageRenderObject(
            0,
            RenderUtil.getScreenWidth(),
            RenderUtil.getScreenHeight(),
            new Area.Position(RenderUtil.getScreenCenter(RenderUtil.getScreenWidth(), RenderUtil.getScreenHeight()), Area.Point.CENTER),
            Padding.NONE,
            new ResourceLocation(MOD_ID, "textures/gui/themes/futuristic/background.png"),
            true,
            (float) 3072/1157,
            ImageRenderObject.FixedParameter.HEIGHT,
            Alignment.CENTERED
    );

    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
        this.theme = ConfigManager.THEME.get().getTheme();
    }

    @Override
    protected void init() {
        this.addRenderableOnly(background);
        this.renderables.stream().filter(renderable -> renderable instanceof RenderObject).forEach(renderable -> ((RenderObject) renderable).onResize(this.width, this.height));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

}
