package de.thedead2.progression_reloaded.criteria.filters.entity;

import com.google.common.collect.Lists;
import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IFilter;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public abstract class FilterBaseEntity implements IFilter<List<EntityLivingBase>, EntityLivingBase> {
    private IFilterProvider provider;

    @Override
    public IFilterProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(IFilterProvider provider) {
        this.provider = provider;
    }

    @Override
    public List<EntityLivingBase> getRandom(EntityPlayer player) {
        return Lists.newArrayList(EntityHelper.getRandomEntity(player.worldObj, this.getProvider()));
    }

    @Override
    public void apply(EntityLivingBase entity) {}

    @Override
    public boolean matches(Object object) {
        if (!(object instanceof EntityLivingBase)) return false;
        return matches((EntityLivingBase) object);
    }

    @Override
    public IFilterType getType() {
        return ProgressionAPI.filters.getEntityFilter();
    }

    protected abstract boolean matches(EntityLivingBase entity);
}