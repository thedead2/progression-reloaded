package de.thedead2.progression_reloaded.crafting.actions;

import de.thedead2.progression_reloaded.api.special.IHasEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;

public class ActionForgeEvent implements IHasEventBus {
    @Override
    public EventBus getEventBus() {
        return MinecraftForge.EVENT_BUS;
    }
}
