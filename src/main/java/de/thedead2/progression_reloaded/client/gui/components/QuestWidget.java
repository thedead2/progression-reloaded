package de.thedead2.progression_reloaded.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.textures.IconInfos;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.client.gui.util.Size;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.tasks.types.QuestTask;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


public class QuestWidget extends ScreenComponent {

    private final ProgressionFont font;

    private final TextBox title;

    private final TextBox description;

    private final SelectionList<QuestTask> goals;

    private QuestProgress quest;


    public QuestWidget(Area area, QuestProgress quest) {
        super(area);
        this.font = FontManager.getInstance().getFont(ModClientInstance.getInstance().getModRenderer().getThemeManager().getActiveTheme().get().font());
        this.title = new TextBox(area.copy().setHeight(MathHelper.percentOf(5, area.getInnerHeight())));
        this.description = new TextBox(area.copy().growX(-(area.getWidth() / 2)).growY(-MathHelper.percentOf(5, area.getInnerHeight())).moveX(area.getWidth() / 2).moveY(-MathHelper.percentOf(5, area.getInnerHeight())));
        this.goals = new SelectionList<>(area.copy()
                                             .growX(-(area.getWidth() / 2))
                                             .growY(-MathHelper.percentOf(5, area.getInnerHeight()))
                                             .moveY(-MathHelper.percentOf(5, area.getInnerHeight())), new Size(area.getWidth() / 2, MathHelper.percentOf(5, area.getInnerHeight())),
                                         (content, poseStack, entryArea, mouseX, mouseY, partialTick) -> {
                                             RenderUtil.renderIcon(poseStack, IconInfos.CROSSED_SWORDS, entryArea.getInnerX(), Alignment.LEFT_CENTERED.getYPos(entryArea, 16, 0), entryArea.getZ(), 16, Color.WHITE.getRGB());
                                             float descriptionStart = entryArea.getInnerX() + 16 + 5;
                                             this.font.drawWithLineWrap(poseStack, content.getDescription(), descriptionStart, Alignment.LEFT_CENTERED.getYPos(entryArea, this.font.height(content.getDescription(), entryArea.getInnerXMax() - descriptionStart), 0), entryArea.getZ(), entryArea.getInnerXMax() - descriptionStart);
                                         }
        );

        this.setQuest(quest);
    }


    public void setQuest(QuestProgress quest) {
        this.quest = quest;

        if(quest != null) {
            QuestDisplayInfo questInfo = quest.getProgressable().getDisplay();

            this.title.setValue(new FormattedString(questInfo.title(), FontFormatting.defaultFormatting().setFont(this.font.getName()).setBold(true).setLetterSpacing(2).setLineHeight(20)));
            this.description.setValue(new FormattedString(questInfo.description(), FontFormatting.defaultFormatting().setFont(this.font.getName()).setLineHeight(12)));
            this.goals.clear();
            this.goals.addAll(quest.getProgressable().getTasks().getTasks().values());
        }
        else {
            this.title.setValue(FormattedString.EMPTY);
            this.description.setValue(FormattedString.EMPTY);
            this.goals.clear();
        }
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);

        this.title.render(poseStack, mouseX, mouseY, partialTick);
        this.description.render(poseStack, mouseX, mouseY, partialTick);
        this.goals.render(poseStack, mouseX, mouseY, partialTick);
    }




    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
