package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.special.ICustomTooltip;
import de.thedead2.progression_reloaded.api.special.IHasEventBus;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;

import java.util.List;

public abstract class RewardBaseAbility extends RewardBaseSingular implements ICustomTooltip, IHasEventBus {
    @Override
    public EventBus getEventBus() {
        return MinecraftForge.EVENT_BUS;
    }

    @Override
    public void addTooltip(List list) {
        list.add(TextFormatting.GOLD + de.thedead2.progression_reloaded.ProgressionReloaded.translate("ability"));
        addAbilityTooltip(list);
    }

    public abstract void addAbilityTooltip(List list);
}
