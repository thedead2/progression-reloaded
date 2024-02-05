package de.thedead2.progression_reloaded.client.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class ProgressionBookGUIEditor extends Screen {

    private final QuestLogScreen gui;


    public ProgressionBookGUIEditor(QuestLogScreen gui) {
        super(Component.literal("ProgressionBookGUI"));
        this.gui = gui;
    }

}
