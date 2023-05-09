package de.thedead2.progression_reloaded.data.trigger;

import de.thedead2.progression_reloaded.data.criteria.ICriterion;
import de.thedead2.progression_reloaded.player.SinglePlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class KillTrigger extends SimpleTrigger implements ICriterion {
    private final EntityType<?> entity;
    private final DamageSource source;

    public KillTrigger(EntityType<?> entity, DamageSource source) {
        this.entity = entity;
        this.source = source;
    }
    public KillTrigger(EntityType<?> entity) {
        this(entity, null);
    }

    @Override
    public SimpleTrigger getTrigger() {
        return this;
    }

    @Override
    public void trigger(SinglePlayer player, Object... data) {
        this.trigger(player, singlePlayer -> {
            EntityType<?> entity = ((LivingEntity) data[0]).getType();
            DamageSource source = (DamageSource) data[1];
            return this.entity.equals(entity) && (this.source == null || this.source.equals(source));
        });
    }
}
