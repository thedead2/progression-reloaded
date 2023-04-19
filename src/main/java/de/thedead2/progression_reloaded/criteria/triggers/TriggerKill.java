package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ProgressionRule(name="kill", color=0xFF000000)
public class TriggerKill extends TriggerBaseEntity {
    @Override
    public ITrigger copy() {
        return copyEntity(new TriggerKill());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(LivingDeathEvent event) {
        Entity source = event.getSource().getSourceOfDamage();
        if (!(source instanceof EntityPlayer)) {
            source = event.getSource().getEntity();
        }

        if (source instanceof EntityPlayer) {
            ProgressionAPI.registry.fireTrigger((EntityPlayer) source, getProvider().getUnlocalisedName(), event.getEntityLiving());
        }
    }
}