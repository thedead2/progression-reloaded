package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ProgressionRule(name="attack", color=0xFF3C3F41)
public class TriggerAttack extends TriggerBaseEntity {
    @Override
    public ITrigger copy() {
        return copyEntity(new TriggerAttack());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEvent(AttackEntityEvent event) {
        if (event.getTarget() instanceof EntityLivingBase) {
            ProgressionAPI.registry.fireTrigger(event.getEntityPlayer(), getProvider().getUnlocalisedName(), event.getTarget());
        }
    }
}