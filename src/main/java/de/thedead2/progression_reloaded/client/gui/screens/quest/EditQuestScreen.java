package de.thedead2.progression_reloaded.client.gui.screens.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.SearchBar;
import de.thedead2.progression_reloaded.client.gui.components.TextBox;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FormattedString;
import de.thedead2.progression_reloaded.client.gui.screens.ProgressionScreen;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;
import de.thedead2.progression_reloaded.data.display.QuestDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;


public class EditQuestScreen extends ProgressionScreen {

    private static final Alignment ALIGNMENT = Alignment.TOP_CENTERED;

    private final ProgressionQuest quest;

    private final QuestDisplayInfo.Builder displayInfoBuilder;


    public EditQuestScreen(Screen parent, ProgressionQuest quest) {
        super(Component.literal("EditQuestScreen"), parent);
        this.quest = quest;
        this.displayInfoBuilder = quest.getDisplay().deconstruct();
    }


    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }


    @Override
    protected void init() {
        SearchBar titleField = new SearchBar(new Area(width -> ALIGNMENT.getXPos(null, width, 0), height -> 50, 0, 100, 20, new Padding(2)), Color.GRAY.getRGB());
        titleField.onEnter(formattedCharSeq -> this.displayInfoBuilder.withName(formattedCharSeq.toString()));
        titleField.setEditable();
        titleField.setValue(new FormattedString(this.quest.getTitle(), FontFormatting.defaultFormatting()));
        titleField.setSuggestion(Component.literal("Title..."));

        TextBox descriptionField = new TextBox(new Area(width -> ALIGNMENT.getXPos(null, width, 0), height -> 50 + titleField.getHeight() + 5, 0, 100, 100, new Padding(2)), Color.GRAY.getRGB());
        descriptionField.setValueListener(formattedCharSeq -> this.displayInfoBuilder.withDescription(formattedCharSeq.toString()));
        descriptionField.setEditable();
        descriptionField.setValue(new FormattedString(this.quest.getDisplay().description(), FontFormatting.defaultFormatting()));
        descriptionField.setSuggestion(Component.literal("Quest description..."));
        descriptionField.setVerticalTextAlignment(Alignment.YAlign.BOTTOM);

        this.addRenderableWidget(titleField);
        this.addRenderableWidget(descriptionField);

        this.setInitialFocus(titleField);
        this.setBackgroundBlur();
    }


    @Override
    public void onClose() {
        super.onClose();
        this.resetBlurEffect();
    }
}
