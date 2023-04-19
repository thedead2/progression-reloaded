package de.thedead2.progression_reloaded.criteria.filters.entity;

import com.google.common.collect.Lists;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IHasEventBus;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import de.thedead2.progression_reloaded.helpers.ListHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@ProgressionRule(name="lastKilled", color=0xFFB25900)
public class FilterLastKilledEntity extends FilterBaseEntity implements IHasEventBus {
    private static final HashMap<UUID, EntityLivingBase> cache = new HashMap();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEvent(LivingDeathEvent event) {
        if (!event.getEntityLiving().worldObj.isRemote) {
            Entity source = event.getSource().getSourceOfDamage();
            if (source instanceof EntityPlayer) {
                cache.put(PlayerHelper.getUUIDForPlayer((EntityPlayer) source), event.getEntityLiving());
            }
        }
    }

    @Override
    public EventBus getEventBus() {
        return MinecraftForge.EVENT_BUS;
    }

    @Override
    public List<EntityLivingBase> getRandom(EntityPlayer player) {
        if (cache.get(PlayerHelper.getUUIDForPlayer(player)) != null) {
            EntityLivingBase existing = cache.get(PlayerHelper.getUUIDForPlayer(player));
            NBTTagCompound tag = new NBTTagCompound();
            existing.writeToNBT(tag);
            return ListHelper.newArrayList(EntityHelper.clone(player.worldObj, existing, tag, this));
        }

        return Lists.newArrayList();
    }

    @Override
    protected boolean matches(EntityLivingBase entity) {
        return true;
    }
}