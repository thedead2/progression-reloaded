package de.thedead2.progression_reloaded.criteria.rewards;

import com.google.common.collect.Lists;
import de.thedead2.progression_reloaded.api.criteria.*;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.criteria.filters.potion.FilterPotionBase;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterFieldPreview;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeEntity;
import de.thedead2.progression_reloaded.gui.filters.FilterTypePotion;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ProgressionRule(name="potioneffect", color=0xFF2C7373)
public class RewardPotion extends RewardBaseItemFilter implements ICustomDescription, ICustomWidth, ICustomTooltip, ISpecialFieldProvider {
    public List<IFilterProvider> targets = new ArrayList();
    public boolean defaultToPlayer = true;
    public boolean randomVanilla = false;
    public int duration = 200;
    public int amplifier = 0;
    public boolean particles = false;

    public RewardPotion() {
        BROKEN = new ItemStack(Items.POTIONITEM, 1, 0);
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.EDIT ? 100 : 55;
    }

    @Override
    public void addTooltip(List list) {
        ItemStack stack = preview == null ? BROKEN : preview;
        Items.POTIONITEM.addInformation(stack, MCClientHelper.getPlayer(), list, false);
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.DISPLAY) fields.add(new ItemFilterFieldPreview("filters", this, 5, 25, 2.8F));
        else {
            fields.add(new ItemFilterField("filters", this));
            fields.add(new ItemFilterField("targets", this));
        }
    }

    @Override
    public List<IFilterProvider> getAllFilters() {
        List<IFilterProvider> list = Lists.newArrayList();
        list.addAll(filters);
        list.addAll(targets);
        return list;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        if (fieldName.equals("targets")) return FilterTypeEntity.INSTANCE;
        return FilterTypePotion.INSTANCE;
    }

    @Override
    public void reward(EntityPlayerMP thePlayer) {
        if (thePlayer != null) {
            Collection<PotionEffect> effects = FilterPotionBase.getRandomEffects(filters);
            if (effects != null && effects.size() > 0) {
                IFilter filter = EntityHelper.getFilter(targets, thePlayer);
                if (filter != null) {
                    List<EntityLivingBase> entities = (List<EntityLivingBase>) filter.getRandom(thePlayer);
                    if (entities.size() == 0 && defaultToPlayer) entities.add(thePlayer);
                    for (EntityLivingBase entity : entities) {
                        for (PotionEffect effect : effects) {
                            if (randomVanilla) entity.addPotionEffect(new PotionEffect(effect));
                            else {
                                entity.addPotionEffect(new PotionEffect(effect.getPotion(), duration, amplifier, false, particles));
                            }
                        }
                    }
                }
            }
        }
    }
}
