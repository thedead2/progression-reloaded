package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.criteria.*;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.IHasFilters;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeEntity;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;

@ProgressionRule(name="setFire", color=0xFFFF4200, icon="minecraft:flint_and_steel")
public class RewardSetFire extends RewardBase implements IHasFilters, ISpecialFieldProvider {
    public List<IFilterProvider> targets = new ArrayList();
    public boolean defaultToPlayer = true;
    public int duration;

    @Override
    public List<IFilterProvider> getAllFilters() {
        return targets;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        return FilterTypeEntity.INSTANCE;
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        fields.add(new ItemFilterField("targets", this));
    }

    @Override
    public void reward(EntityPlayerMP thePlayer) {
        IFilter filter = EntityHelper.getFilter(targets, thePlayer);
        if (filter != null) {
            List<EntityLivingBase> entities = (List<EntityLivingBase>) filter.getRandom(thePlayer);
            if (entities.size() == 0) entities.add(thePlayer);
            if (entities.size() == 0 && defaultToPlayer) entities.add(thePlayer);
            for (EntityLivingBase entity : entities) {
                entity.setFire(duration);
            }
        }
    }
}