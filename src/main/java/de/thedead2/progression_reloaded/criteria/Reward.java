package de.thedead2.progression_reloaded.criteria;

import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.api.criteria.IReward;
import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.helpers.JSONHelper;
import de.thedead2.progression_reloaded.helpers.SplitHelper;
import de.thedead2.progression_reloaded.json.Options;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class Reward implements IRewardProvider {
    private final IReward reward;
    private final String unlocalised;
    private final int color;

    private ICriteria criteria;
    private UUID uuid;

    private ItemStack stack;
    public boolean isVisible;
    public boolean mustClaim;
    public boolean onePerTeam;

    //Dummy constructor for storing the default values
    public Reward(IReward reward, String unlocalised, int color) {
        this.reward = reward;
        this.unlocalised = unlocalised;
        this.color = color;
        this.reward.setProvider(this);
    }

    public Reward (ICriteria criteria, UUID uuid, IReward reward, ItemStack stack, String unlocalised, int color) {
        this.criteria = criteria;
        this.uuid = uuid;
        this.reward = reward;
        this.unlocalised = unlocalised;
        this.color = color;
        this.stack = stack;
        this.isVisible = true;
        this.mustClaim = Options.mustClaimDefault;
        this.reward.setProvider(this);
        this.onePerTeam = onePerTeam;
    }

    @Override
    public ICriteria getCriteria() {
        return criteria;
    }

    @Override
    public IReward getProvided() {
        return reward;
    }

    @Override
    public String getUnlocalisedName() {
        return unlocalised;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public ItemStack getIcon() {
        return reward instanceof ICustomIcon ? ((ICustomIcon)reward).getIcon() : stack;
    }

    @Override
    public String getLocalisedName() {
        return reward instanceof ICustomDisplayName ? ((ICustomDisplayName)reward).getDisplayName() : de.thedead2.progression_reloaded.ProgressionReloaded.translate(getUnlocalisedName());
    }

    @Override
    public String getDescription() {
        return reward instanceof ICustomDescription ? ((ICustomDescription)reward).getDescription() : de.thedead2.progression_reloaded.ProgressionReloaded.format(getUnlocalisedName() + ".description");
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return reward instanceof ICustomWidth ? ((ICustomWidth)reward).getWidth(mode) : 100;
    }

    @Override
    public void addTooltip(List list) {
        if (reward instanceof ICustomTooltip) ((ICustomTooltip)reward).addTooltip(list);
        else{
            for (String s : SplitHelper.splitTooltip(getDescription(), 42)) {
                list.add(s);
            }
        }
    }

    @Override
    public UUID getUniqueID() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    @Override
    public IRewardProvider setIcon(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean mustClaim() {
        return mustClaim;
    }

    @Override
    public boolean isOnePerTeam() {
        return onePerTeam;
    }

    @Override
    public void readFromJSON(JsonObject data) {
        isVisible = JSONHelper.getBoolean(data, "isVisible", true);
        mustClaim = JSONHelper.getBoolean(data, "mustClaim", false);
        onePerTeam = JSONHelper.getBoolean(data, "onePerTeam", false);
    }

    @Override
    public void writeToJSON(JsonObject data) {
        JSONHelper.setBoolean(data, "isVisible", isVisible, true);
        JSONHelper.setBoolean(data, "mustClaim", mustClaim, false);
        JSONHelper.setBoolean(data, "onePerTeam", onePerTeam, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IRewardProvider)) return false;

        IRewardProvider that = (IRewardProvider) o;
        return getUniqueID() != null ? getUniqueID().equals(that.getUniqueID()) : that.getUniqueID() == null;

    }

    @Override
    public int hashCode() {
        return getUniqueID() != null ? getUniqueID().hashCode() : 0;
    }
}
