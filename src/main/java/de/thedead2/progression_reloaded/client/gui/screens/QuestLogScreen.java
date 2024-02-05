package de.thedead2.progression_reloaded.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.QuestWidget;
import de.thedead2.progression_reloaded.client.gui.components.SelectionList;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.textures.IconInfos;
import de.thedead2.progression_reloaded.client.gui.util.*;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


//nice gradient: new GradientColor(2, 0, 36, 255, 0.08f), new GradientColor(9, 9, 121, 255, 0.22f), new GradientColor(0, 212, 255, 255, 0.46f), new GradientColor(255, 0, 0, 255, 0.88f), new GradientColor(139, 0, 255, 255, 1f));
public class QuestLogScreen extends ProgressionScreen {

    private final PlayerQuests playerQuests;


    public QuestLogScreen(PlayerQuests playerQuests) {
        super(Component.literal("QuestLogScreen"), null);
        this.playerQuests = playerQuests;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }


    @Override
    protected void init() {
        float listWidth = MathHelper.percentOf(25f, this.width);

        QuestWidget questWidget = this.addRenderableWidget(new QuestWidget(new Area(listWidth, 0, 0, this.width - listWidth, this.height, new Padding(MathHelper.percentOf(12.5f, listWidth), MathHelper.percentOf(5, this.height))), this.playerQuests.getFollowedQuestProgress()));
        this.addRenderableWidget(new SelectionList<ProgressionQuest>(new Area(0, 0, 0, listWidth, this.height, new Padding(MathHelper.percentOf(12.5f, listWidth), MathHelper.percentOf(5, this.height))), new Size(listWidth, Mth.clamp(MathHelper.percentOf(5, this.height), 20, 100)), (content, poseStack, entryArea, mouseX, mouseY, partialTick) -> {
            entryArea.setPadding(MathHelper.percentOf(5, entryArea.getWidth()), MathHelper.percentOf(5, entryArea.getHeight()));
            RenderUtil.linearGradient(poseStack, entryArea.getX(), entryArea.getXMax(), entryArea.getY(), entryArea.getYMax(), entryArea.getZ(), 45, new GradientColor(2, 0, 36, 255, 0), new GradientColor(2, 0, 36, 38, 0.5f), new GradientColor(2, 0, 36, 255, 1));
            Area imageArea = entryArea.copy().setZ(1).setWidth(MathHelper.percentOf(25, entryArea.getWidth())).align(Alignment.LEFT_CENTERED, entryArea);
            RenderUtil.renderArea(poseStack, imageArea, Color.GREEN.getRGB(), Color.RED.getRGB());
            RenderUtil.renderIcon(poseStack, IconInfos.CROSSED_SWORDS, imageArea.getX(), imageArea.getY(), imageArea.getZ(), imageArea.getHeight(), Color.WHITE.getRGB());
            float itemSize = 1 * RenderUtil.getGuiScale();
            RenderUtil.renderItem(content.getDisplay().icon(), Alignment.CENTERED.getXPos(imageArea, itemSize, 0), Alignment.CENTERED.getYPos(imageArea, itemSize, 0), imageArea.getZ() + 1, itemSize);
            Area textArea = entryArea.copy().setWidth(MathHelper.percentOf(75, entryArea.getWidth())).setX(imageArea.getXMax()).setZ(1);
            TextBox text = new TextBox(textArea, new FormattedString(content.getTitle(), this.font.getName(), FontFormatting.defaultFormatting().setLineHeight(5).setLetterSpacing(2), false));
            text.setVerticalTextAlignment(Alignment.YAlign.CENTER);
            text.render(poseStack, mouseX, mouseY, partialTick);
            RenderUtil.border(poseStack, entryArea.getX(), entryArea.getXMax(), entryArea.getY(), entryArea.getYMax(), entryArea.getZ() + 5, 2, 0, Color.BLACK.getRGB());
        }, (content, mouseX, mouseY, button) -> {
            questWidget.setQuest(this.playerQuests.getOrStartProgress(content));
            return true;
        }, Color.BLACK.getRGB())).addAll(this.playerQuests.getStartedOrActiveQuests());
    }
}
