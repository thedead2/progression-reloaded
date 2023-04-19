package de.thedead2.progression_reloaded.criteria.filters.entity;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import de.thedead2.progression_reloaded.helpers.ListHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

@ProgressionRule(name="displayName", color=0xFFB25900)
public class FilterEntityDisplayName extends FilterBaseEntity implements IInit {
    private String checkName = "Girafi";
    private boolean matchBoth;
    private boolean matchFront;
    private boolean matchBack;

    public String entityName = "Girafi";

    @Override
    public void init(boolean isClient) {
        if (entityName.startsWith("*")) matchFront = true;
        else matchFront = false;
        if (entityName.endsWith("*")) matchBack = true;
        else matchBack = false;
        matchBoth = matchFront && matchBack;
        checkName = entityName.replaceAll("\\*", "");
    }

    @Override
    public List<EntityLivingBase> getRandom(EntityPlayer player) {
        return ListHelper.newArrayList(EntityHelper.getRandomEntity(player.worldObj, null));
    }

    @Override
    public void apply(EntityLivingBase entity) {
        entity.setCustomNameTag(checkName);
    }

    @Override
    protected boolean matches(EntityLivingBase entity) {
        String name = entity.getName();
        if (matchBoth && name.toLowerCase().contains(checkName.toLowerCase())) return true;
        else if (matchFront && !matchBack && name.toLowerCase().endsWith(checkName.toLowerCase())) return true;
        else if (!matchFront && matchBack && name.toLowerCase().startsWith(checkName.toLowerCase())) return true;
        else if (name.toLowerCase().equals(checkName.toLowerCase())) return true;
        else return false;
    }
}