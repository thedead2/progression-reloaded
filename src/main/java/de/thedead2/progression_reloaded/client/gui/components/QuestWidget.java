package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import net.minecraft.client.gui.GuiComponent;


public class QuestWidget extends GuiComponent {

    private final ProgressionTheme theme;

    private final ProgressionQuest quest;
    //frame 2 breite/ höhe 830 px
    //start:


    public QuestWidget(ProgressionTheme theme, ProgressionQuest quest) {
        this.theme = theme;
        this.quest = quest;
    }


    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        //RenderSystem.setShaderTexture(0, theme.getWidgets());
        //blit(poseStack, screen.getScreenCenterX(), screen.getScreenCenterY(), 0, 0, 100, 100, 100, 100);
    }
}
