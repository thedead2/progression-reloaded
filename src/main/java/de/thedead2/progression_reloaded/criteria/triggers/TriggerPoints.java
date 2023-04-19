package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.EventBus;

import java.util.UUID;

@ProgressionRule(name="points", color=0xFFB2B200, meta="onReceivedPoints")
public class TriggerPoints extends TriggerBaseBoolean implements ICustomDescription {
    public String variable = "gold";
    public double amount = 1D;
    public boolean consume = true;

    @Override
    public ITrigger copy() {
        TriggerPoints trigger = new TriggerPoints();
        trigger.variable = variable;
        trigger.amount = amount;
        trigger.consume = consume;
        return copyBoolean(trigger);
    }

    @Override
    public String getDescription() {
        String extra = consume ? "\n" + TextFormatting.ITALIC + de.thedead2.progression_reloaded.ProgressionReloaded.format("trigger.points.extra", variable) : "";
        String value = (amount == (long) amount) ? String.format("%d", (long) amount): String.format("%s", amount);
        return de.thedead2.progression_reloaded.ProgressionReloaded.format("trigger.points.description", value, variable, extra);
    }
    
    @Override
    public EventBus getEventBus() {
        return null;
    }

    @Override
    public boolean onFired(UUID uuid, Object... data) {
        double total = ProgressionAPI.player.getDouble(uuid, variable, false);
        if (total >= amount) {
            markTrue();
            if (consume) {
                PlayerTracker.getServerPlayer(uuid).addDouble(variable, -amount);
            }
        }

        return true;
    }
}
