package de.thedead2.progression_reloaded.client.gui.screens;

import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class LevelInformationScreen extends Screen {

    private final ProgressionLevel level;


    public LevelInformationScreen(ResourceLocation levelId) {
        super(Component.empty());
        this.level = ModRegistries.LEVELS.get().getValue(levelId);
    }
}
