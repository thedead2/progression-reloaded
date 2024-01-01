package de.thedead2.progression_reloaded.client.gui.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedCharSeq;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class QuestOverlay extends ProgressOverlay<ProgressionQuest> {
    private final TextBox textBox;


    public QuestOverlay(Area area, QuestDisplayInfo displayInfo, QuestProgress questProgress, @Nullable TextureInfo backgroundFrame, ResourceLocation font) {
        super(area, displayInfo, backgroundFrame, font);
        this.textBox = new TextBox(this.area);

        this.updateProgress(questProgress);
    }


    @Override
    public void updateProgress(IProgressInfo<ProgressionQuest> progressInfo) {
        FormattedString title = new FormattedString(this.displayInfo.title(), this.font.getName(), FontFormatting.defaultFormatting().setLineHeight(4).setTextAlignment(Alignment.CENTERED).setBgColor(Color.BLACK.getRGB()).setBgAlpha(0.25f).setColor(153, 102, 51, 255), false);
        List<FormattedString> list = ((QuestProgress) progressInfo).getCurrentDescriptions()
                                                       .stream()
                                                       .map(component -> new FormattedString(component, this.font.getName(), FontFormatting.defaultFormatting().setLineHeight(3).setTextAlignment(Alignment.CENTERED).setBgColor(Color.BLACK.getRGB()).setBgAlpha(0.25f), false))
                                                       .toList();

        List<FormattedString> strings = new ArrayList<>(list);
        strings.add(0, title);

        this.textBox.setValue(new FormattedCharSeq(strings));
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(this.backgroundFrame != null) {
            this.backgroundFrame.render(poseStack, mouseX, mouseY, partialTick);
        }

        this.textBox.render(poseStack, mouseX, mouseY, partialTick);
    }


    public boolean isQuestFollowed(ResourceLocation questId) {
        return this.displayInfo.id().equals(questId);
    }


    public ResourceLocation getFollowedQuest() {
        return this.displayInfo.id();
    }
}
