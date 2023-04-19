package de.thedead2.progression_reloaded.criteria.filters.entity;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.helpers.ListHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

@ProgressionRule(name="online", color=0xFFB25900)
public class FilterOnlinePlayers extends FilterBaseEntity {
    @Override
    public List<EntityLivingBase> getRandom(EntityPlayer player) {
        return ListHelper.newArrayList(PlayerHelper.getAllPlayers());
    }

    @Override
    protected boolean matches(EntityLivingBase entity) {
        return entity instanceof EntityPlayer;
    }
}