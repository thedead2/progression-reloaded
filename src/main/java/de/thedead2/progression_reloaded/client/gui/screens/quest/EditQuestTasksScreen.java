package de.thedead2.progression_reloaded.client.gui.screens.quest;

import de.thedead2.progression_reloaded.client.gui.screens.ProgressionScreen;
import de.thedead2.progression_reloaded.data.quest.QuestTasks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class EditQuestTasksScreen extends ProgressionScreen {

    private final QuestTasks.Builder taskBuilder;


    public EditQuestTasksScreen(Screen parent, QuestTasks tasks) {
        super(Component.literal("EditQuestTasksScreen"), parent);
        this.taskBuilder = tasks.deconstruct();
    }
}
