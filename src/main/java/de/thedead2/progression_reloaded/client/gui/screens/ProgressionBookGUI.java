package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.util.*;
import de.thedead2.progression_reloaded.client.gui.util.objects.ImageRenderObject;
import de.thedead2.progression_reloaded.client.gui.util.objects.RenderObject;
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
            new Area.Position(RenderUtil.getScreenCenter(), Area.Point.CENTER),
            Padding.NONE,
            PoseStackTransformer.NONE,
            new ResourceLocation(MOD_ID, "textures/gui/themes/futuristic/background.png"),
            true,
            (float) 4476 / 1953,
            ImageRenderObject.FixedParameter.HEIGHT, //TODO: Picture still gets stretched --> why?
            Alignment.CENTERED
    );

    private final ImageRenderObject frame = new ImageRenderObject(
            1,
            RenderUtil.getScreenWidth(),
            RenderUtil.getScreenHeight(),
            new Area.Position(RenderUtil.getScreenCenter(), Area.Point.CENTER),
            new Padding(0, 0), //TODO: With Padding strangely projected like AnchorPoint is A
            PoseStackTransformer.NONE,
            new ResourceLocation(MOD_ID, "textures/gui/themes/futuristic/futuristic_logo_bright_text.png"),
            true,
            (float) 13731 / 9250,
            ImageRenderObject.FixedParameter.WIDTH,
            Alignment.CENTERED
    );


    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
        this.theme = ConfigManager.THEME.get().getTheme();
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        //ModHelper.LOGGER.debug(background.isInRenderArea(mouseX, mouseY) + " --> " + background.isInArea(mouseX, mouseY));
        super.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    protected void init() {
        //this.addRenderableOnly(background);
        //this.addRenderableOnly(frame);
        this.renderables.stream()
                        .filter(renderable -> renderable instanceof RenderObject)
                        .forEach(renderable -> ((RenderObject) renderable).onResize(this.width, this.height));
    }

}
