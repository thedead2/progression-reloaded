package de.thedead2.progression_reloaded.client.gui;

import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ProgressionBookGUI extends Screen {
    public ProgressionBookGUI() {
        super(Component.literal("ProgressionBookGUI"));
    }

    @Override
    protected void init() {
        addRenderableWidget(new Button.Builder(Component.literal("Test"), (button) -> ModHelper.LOGGER.info("Test")).bounds(50, 50, 100, 20).build());
    }
}
