package de.thedead2.progression_reloaded.criteria.triggers;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

@ProgressionRule(name="achievement", color=0xFF00D9D9, meta="onReceivedAchiement")
public class TriggerAchievement extends TriggerBaseBoolean implements IInit, ICustomWidth, ISpecialFieldProvider, IItemGetterCallback, IAdditionalTooltip<ItemStack> {
    public String id = "openInventory";
    private transient Achievement achievement;

    @Override
    public ITrigger copy() {
        TriggerAchievement trigger = new TriggerAchievement();
        trigger.id = id;
        return copyBoolean(trigger);
    }

    @Override
    public void init(boolean isClient) {
        for (Achievement a: AchievementList.ACHIEVEMENTS) {
            if (a.statId.equals("achievement." + id)) {
                achievement = a;
                break;
            }
        }
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.EDIT ? 100 : 70;
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.DISPLAY) fields.add(ProgressionAPI.fields.getItem(this, "id", 20, 42, 2F));
    }

    @Override
    public ItemStack getItem(String fieldName) {
        return achievement != null ? achievement.theItemStack : new ItemStack(Items.GOLDEN_HOE);
    }

    @Override
    public void addHoverTooltip(String field, ItemStack stack, List<String> tooltip) {
        tooltip.clear();
        if (achievement != null) {
            tooltip.add(TextFormatting.DARK_AQUA + I18n.translateToLocal(achievement.statId));
            String[] split = WordUtils.wrap(achievement.getDescription() + ".", 27).split("\n");
            for (String s: split) {
                tooltip.add(s.trim());
            }
        }
    }
    
    @SubscribeEvent
    public void onAchievementGet(AchievementEvent event) {
        ProgressionAPI.registry.fireTrigger(event.getEntityPlayer(), getProvider().getUnlocalisedName(), event.getAchievement());
    }

    @Override
    protected boolean isTrue(Object... data) {
        return ((Achievement) data[0]).statId.equals("achievement." + id);
    }
}
