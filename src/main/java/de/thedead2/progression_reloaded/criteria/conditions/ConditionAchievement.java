package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

@ProgressionRule(name="achievement", color=0xFFFFFF00, meta="ifHasAchievement")
public class ConditionAchievement extends ConditionBase implements IInit, ICustomWidth, ISpecialFieldProvider, IItemGetterCallback, IAdditionalTooltip<ItemStack> {
    public String id = "mineWood";
    private transient Achievement achievement;

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
        return mode == DisplayMode.EDIT ? 100 : 92;
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
    
    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        if (achievement != null) {
            for (EntityPlayer player: team.getTeamEntities()) { //If any team member has the achievement
                if (player.worldObj.isRemote && ((EntityPlayerSP)player).getStatFileWriter().hasAchievementUnlocked(achievement)) return true;
                else if (!player.worldObj.isRemote && ((EntityPlayerMP)player).getStatFile().hasAchievementUnlocked(achievement)) return true;
            }
        }

        return false;
    }
}
