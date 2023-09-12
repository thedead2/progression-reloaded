package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.client.gui.util.objects.ImageRenderObject;
import de.thedead2.progression_reloaded.client.gui.util.objects.RenderObject;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class ProgressionBookGUI extends Screen {

    private final Player player;

    private final ProgressionTheme theme;

    private RenderObject renderObject;

    private float rot = 1f;


    public ProgressionBookGUI(Player player) {
        super(Component.literal("ProgressionBookGUI"));
        this.player = player;
        this.theme = ConfigManager.THEME.get().getTheme();
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        //TODO: When rotating backside doesn't get rendered
        renderObject.rotateY(Axis.YN.rotationDegrees(rot += 1f));
        /*Vector3f anchor = renderObject.getPositionAnchor();
        renderObject.changePosition(new Area.Position(anchor.x <= this.width ? anchor.x + 1 : anchor.x - this.width, anchor.y, anchor.z, renderObject.getAnchorPoint()));
        */
        ModHelper.LOGGER.debug(renderObject.isInArea(mouseX, mouseY));
        super.render(poseStack, mouseX, mouseY, partialTick);
        RenderUtil.renderObjectOutline(poseStack, renderObject);
    }


    @Override
    protected void init() {
        renderObject = new ImageRenderObject((float) this.width / 2, (float) this.height / 2, 0, Area.AnchorPoint.CENTER, 300, 150, new Padding(10),
                                             new ResourceLocation(MOD_ID, "textures/gui/themes/futuristic/pr_logo_futuristic_bg.png")
        );
        this.addRenderableOnly(renderObject);
        /*if(renderObject instanceof ImageRenderObject imageRenderObject){
            imageRenderObject.enableRatioKeeping((float) 3072 /2051, ImageRenderObject.FixedParameter.WIDTH, Alignment.CENTERED);
        }*/
    }
}
