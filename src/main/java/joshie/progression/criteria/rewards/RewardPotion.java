package joshie.progression.criteria.rewards;

import joshie.progression.api.criteria.IProgressionFilterSelector;
import joshie.progression.api.special.ISpecialFilters;
import joshie.progression.gui.filters.FilterSelectorPotion;
import joshie.progression.helpers.ItemHelper;
import joshie.progression.helpers.MCClientHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.List;

public class RewardPotion extends RewardBaseItemFilter implements ISpecialFilters {
    public boolean randomVanilla = false;
    public int customid = -1;
    public int duration = 200;
    public int amplifier = 0;
    public boolean particles = false;

    public RewardPotion() {
        super("potioneffect", 0xFF2C7373);
        BROKEN = new ItemStack(Items.potionitem, 1, 0);
    }

    @Override
    public IProgressionFilterSelector getFilterForField(String fieldName) {
        return FilterSelectorPotion.INSTANCE;
    }

    @Override
    public void reward(EntityPlayerMP player) {
        if (player != null) {
            ItemStack stack = ItemHelper.getRandomItem(filters, null);
            if (stack != null) {
                for (PotionEffect effect : Items.potionitem.getEffects(stack)) {
                    if (randomVanilla) player.addPotionEffect(new PotionEffect(effect));
                    else {
                        int id = customid >= 0 ? customid : effect.getPotionID();
                        player.addPotionEffect(new PotionEffect(id, duration, amplifier, false, particles));
                    }
                }
            }
        }
    }

    @Override
    public void addTooltip(List list) {
        ItemStack stack = preview == null ? BROKEN : preview;
        Items.potionitem.addInformation(stack, MCClientHelper.getPlayer(), list, false);
    }
}
