package de.thedead2.progression_reloaded.criteria;

import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.criteria.IConditionProvider;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.api.criteria.ITrigger;
import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.helpers.JSONHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraft.util.text.TextFormatting.*;

public class Trigger implements ITriggerProvider {
    private final ITrigger trigger;
    private final String unlocalised;
    private final int color;

    private ICriteria criteria;
    private UUID uuid;

    private ItemStack stack;
    private List<IConditionProvider> conditions;

    private boolean isCancelable = false;
    public boolean isCanceling = false;
    public boolean isVisible = true;

    //Dummy constructor for storing the default values
    public Trigger(ITrigger trigger, String unlocalised, int color) {
        this.trigger = trigger;
        this.unlocalised = unlocalised;
        this.color = color;
        this.trigger.setProvider(this);
    }

    public Trigger(ICriteria criteria, UUID uuid, ITrigger trigger, ItemStack stack, String unlocalised, int color, boolean isCancelable) {
        this.criteria = criteria;
        this.uuid = uuid;
        this.trigger = trigger;
        this.unlocalised = unlocalised;
        this.color = color;
        this.stack = stack;
        this.trigger.setProvider(this);
        this.conditions = new ArrayList();
        this.isCancelable = isCancelable;
    }

    @Override
    public ITriggerProvider copy() {
        Trigger copy = new Trigger(criteria, uuid, trigger.copy(), stack, unlocalised, color, isCancelable);
        copy.conditions = conditions; //We can point to the same conditions as it doesn't matter
        copy.isCanceling = isCanceling;
        copy.isVisible = isVisible;
        return copy;
    }

    @Override
    public ICriteria getCriteria() {
        return criteria;
    }

    @Override
    public ITrigger getProvided() {
        return trigger;
    }

    @Override
    public List<IConditionProvider> getConditions() {
        return conditions;
    }

    @Override
    public boolean isCancelable() {
        return isCancelable;
    }

    public boolean isCanceling() {
        return isCanceling;
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
        ItemStack result = trigger instanceof ICustomIcon ? ((ICustomIcon)trigger).getIcon(): stack;
        return result == null ? stack: result;
    }

    @Override
    public String getLocalisedName() {
        return trigger instanceof ICustomDisplayName ? ((ICustomDisplayName)trigger).getDisplayName() : de.thedead2.progression_reloaded.ProgressionReloaded.translate(getUnlocalisedName());
    }

    private String getTriggerDescription() {
        String ret = (trigger instanceof ICustomDescription ? ((ICustomDescription)trigger).getDescription() : de.thedead2.progression_reloaded.ProgressionReloaded.format(getUnlocalisedName() + ".description")) + "\n\n";
        if (trigger instanceof ICountable) {
            ICountable countable = ((ICountable)trigger);
            ret = ret + BLUE + countable.getCounter() + "/" + countable.getRequirement() + RESET + "\n\n";
        }

        return ret;
    }

    @Override
    public String getDescription() {
        if(isCancelable && isCanceling) return de.thedead2.progression_reloaded.ProgressionReloaded.format(getUnlocalisedName() + ".cancel");
        else return getTriggerDescription() + GREEN + de.thedead2.progression_reloaded.ProgressionReloaded.format("completed", trigger.getPercentage());
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return trigger instanceof ICustomWidth ? ((ICustomWidth)trigger).getWidth(mode) : 100;
    }

    @Override
    public UUID getUniqueID() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    @Override
    public ITriggerProvider setIcon(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public ITriggerProvider setCancelable() {
        this.isCancelable = true;
        return this;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void readFromJSON(JsonObject data) {
        isVisible = JSONHelper.getBoolean(data, "isVisible", true);
        isCanceling = JSONHelper.getBoolean(data, "isCanceling", false);
    }

    @Override
    public void writeToJSON(JsonObject data) {
        JSONHelper.setBoolean(data, "isVisible", isVisible, true);
        JSONHelper.setBoolean(data, "isCanceling", isCanceling, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ITriggerProvider)) return false;

        ITriggerProvider that = (ITriggerProvider) o;
        return getUniqueID() != null ? getUniqueID().equals(that.getUniqueID()) : that.getUniqueID() == null;

    }

    @Override
    public int hashCode() {
        return getUniqueID() != null ? getUniqueID().hashCode() : 0;
    }
}
