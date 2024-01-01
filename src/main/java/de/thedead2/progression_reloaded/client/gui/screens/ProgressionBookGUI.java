package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.themes.ThemeManager;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.ObjectFit;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


//nice gradient: new GradientColor(2, 0, 36, 255, 0.08f), new GradientColor(9, 9, 121, 255, 0.22f), new GradientColor(0, 212, 255, 255, 0.46f), new GradientColor(255, 0, 0, 255, 0.88f), new GradientColor(139, 0, 255, 255, 1f));
public class ProgressionBookGUI extends ProgressionScreen {
    private ProgressionFont font;

    private final float i = 0;


    public ProgressionBookGUI() {
        super(Component.literal("ProgressionBookGUI"), null);
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        /*RenderUtil.linearGradient(poseStack, 0, 200, 75, 200, 0, i+=0.1f, new GradientColor(2, 0, 36, 255, 0.08f), new GradientColor(9, 9, 121, 255, 0.22f), new GradientColor(0, 212, 255, 255, 0.46f), new GradientColor(255, 0, 0, 255, 0.88f), new GradientColor(139, 0, 255, 255, 1f));
        RenderUtil.radialGradient(poseStack, 200, 400, 75, 200, 0, new GradientColor(2, 0, 36, 255, 0.08f), new GradientColor(9, 9, 121, 255, 0.22f), new GradientColor(0, 212, 255, 255, 0.46f), new GradientColor(255, 0, 0, 255, 0.88f), new GradientColor(139, 0, 255, 255, 1f));
        */
        super.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    protected void init() {
        ThemeManager themeManager = ModClientInstance.getInstance().getModRenderer().getThemeManager();
        this.font = FontManager.getInstance().getFont(themeManager.getActiveTheme().get().font());

        this.addRenderableOnly(new DrawableTexture(themeManager.getActiveTheme().get().backgroundFrame(), new Area(0, 0, 0, this.width, this.height, new Padding(25))));

        this.addRenderableOnly(new DrawableTexture(new TextureInfo(new ResourceLocation("textures/gui/advancements/backgrounds/adventure.png"), Component.empty(), 16, 16, ObjectFit.FILL), new Area(50, 50, 5, 200, 100)));
        /*this.addRenderableWidget(new ExpandableScreenComponent<>(new Area(50, 50, 0, 100, 150), 20, this.font, (area, list) -> {
            SearchBar searchBar = new SearchBar(area.setPadding(0.25f), new FormattedString(Component.literal("Items:"), FontFormatting.defaultFormatting()), Color.GRAY.getRGB());
            searchBar.setEditable();
            searchBar.setValueListener(formattedCharSeq -> list.filter(item -> ForgeRegistries.ITEMS.getKey(item).toString().contains(formattedCharSeq.toString())));
            return searchBar;
        }, area -> {
            SelectionList<Item> list = new SelectionList<>(area, new Size(30, 25), (content, poseStack, xMin, yMin, zPos, width1, height1, mouseX, mouseY, partialTick) -> {
                this.font.draw(poseStack, "A", xMin + 5, yMin + 5, zPos);
                RenderUtil.renderItem(content.getDefaultInstance(), Alignment.CENTERED.getXPos(xMin, width1, 16, 0), Alignment.CENTERED.getYPos(yMin, height1, 16, 0), zPos, 16);
                RenderUtil.renderSquareOutline(poseStack, xMin, xMin + width1, yMin, yMin + height1, zPos, Color.GRAY.getRGB());
            }, Color.GRAY.getRGB());
            list.addAll(ForgeRegistries.ITEMS.getValues());
            return list;
        }));*/
        //this.addRenderableWidget(new GuiButton(new Area(50, 50, 0, 50, 10), new FormattedString("Edit Quests", FontFormatting.defaultFormatting()), null, Color.GRAY.getRGB(), button -> Minecraft.getInstance().setScreen(new EditQuestScreen(this, TestQuests.TEST1))));
    }
}
