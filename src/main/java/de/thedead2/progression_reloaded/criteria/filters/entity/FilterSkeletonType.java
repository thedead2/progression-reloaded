package de.thedead2.progression_reloaded.criteria.filters.entity;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.helpers.ListHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

@ProgressionRule(name="witherskeleton", color=0xFFB25900)
public class FilterSkeletonType extends FilterBaseEntity {
    public boolean wither = true;

    @Override
    public List<EntityLivingBase> getRandom(EntityPlayer player) {
        return ListHelper.newArrayList(new EntitySkeleton(player.worldObj));
    }

    @Override
    public void apply(EntityLivingBase entity) {
        if (entity instanceof EntitySkeleton) {
            EntitySkeleton skeleton = ((EntitySkeleton) entity);
            if (wither) skeleton.setSkeletonType(1);
            else skeleton.setSkeletonType(0);
        }
    }

    @Override
    protected boolean matches(EntityLivingBase entity) {
        if (!(entity instanceof EntitySkeleton)) return false;
        if (wither) return ((EntitySkeleton) entity).getSkeletonType() == 1;
        else return ((EntitySkeleton) entity).getSkeletonType() == 0;
    }
}