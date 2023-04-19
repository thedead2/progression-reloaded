package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.gui.filters.FilterTypePotion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

import java.util.List;

@ProgressionRule(name="potioneffect", color=0xFFFFFF00)
public class ConditionHasPotionEffect extends ConditionBaseItemFilter implements ICustomDescription, ISpecialFieldProvider {
    public String description = "Have the regeneration potion effect";
    public int lessThanFalse = 220;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        return FilterTypePotion.INSTANCE;
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) {
            fields.add(new ItemFilterField("filters", this));
        }
    }

    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        for (EntityPlayer player: team.getTeamEntities()) {
            for (PotionEffect effect: player.getActivePotionEffects()) {
                if (effect.getDuration() > lessThanFalse) {
                    for (IFilterProvider filter : filters) {
                        if (filter.getProvided().matches(effect)) return true;
                    }
                }
            }
        }

        return false;
    }
}
