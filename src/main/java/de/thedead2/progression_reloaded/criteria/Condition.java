package de.thedead2.progression_reloaded.criteria;

import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.criteria.ICondition;
import de.thedead2.progression_reloaded.api.criteria.IConditionProvider;
import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.helpers.JSONHelper;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketIsSatisfied;
import net.minecraft.item.ItemStack;

import java.util.UUID;

import static net.minecraft.util.text.TextFormatting.GREEN;
import static net.minecraft.util.text.TextFormatting.RED;

public class Condition implements IConditionProvider {
    private final ICondition condition;
    private final String unlocalised;

    private ITriggerProvider provider;
    private UUID uuid;

    private ItemStack stack;

    public boolean isVisible = true;
    public boolean inverted = false;

    //Dummy constructor for storing the default values
    public Condition(ICondition condition, String unlocalised) {
        this.condition = condition;
        this.unlocalised = unlocalised;
        this.condition.setProvider(this);
    }

    public Condition(ITriggerProvider trigger, UUID uuid, ICondition condition, ItemStack stack, String unlocalised) {
        this.provider = trigger;
        this.uuid = uuid;
        this.condition = condition;
        this.unlocalised = unlocalised;
        this.stack = stack;
        this.condition.setProvider(this);
    }

    @Override
    public ITriggerProvider getTrigger() {
        return provider;
    }

    @Override
    public ICondition getProvided() {
        return condition;
    }

    @Override
    public String getUnlocalisedName() {
        return unlocalised;
    }

    @Override
    public int getColor() {
        return getTrigger().getColor();
    }

    @Override
    public ItemStack getIcon() {
        return condition instanceof ICustomIcon ? ((ICustomIcon)condition).getIcon() : stack;
    }

    @Override
    public String getLocalisedName() {
        return condition instanceof ICustomDisplayName ? ((ICustomDisplayName)condition).getDisplayName() : de.thedead2.progression_reloaded.ProgressionReloaded.translate(getUnlocalisedName());
    }

    private transient boolean isTrue = false;
    private transient int checkTick = 0;

    @Override
    public void setSatisfied(boolean isTrue) {
        this.isTrue = isTrue;
    }

    @Override
    public boolean isSatisfied() {
        if (checkTick %200 == 0) {
            PacketHandler.sendToServer(new PacketIsSatisfied(uuid));
        }

        checkTick++;
        return isTrue;
    }

    private String getConditionDescription() {
        if (condition instanceof ICustomDescription) return ((ICustomDescription)condition).getDescription();
        if (inverted) return de.thedead2.progression_reloaded.ProgressionReloaded.translate(getUnlocalisedName() + ".description.inverted");
        else return de.thedead2.progression_reloaded.ProgressionReloaded.translate(getUnlocalisedName() + ".description");
    }

    @Override
    public String getDescription() {
        boolean value = inverted ? !isSatisfied() : isSatisfied();
        if (value) {
            return getConditionDescription() + "\n\n" + GREEN + de.thedead2.progression_reloaded.ProgressionReloaded.format("truth", true);
        } else return getConditionDescription() + "\n\n" + RED + de.thedead2.progression_reloaded.ProgressionReloaded.format("truth", false);
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return condition instanceof ICustomWidth ? ((ICustomWidth)condition).getWidth(mode) : 100;
    }

    @Override
    public UUID getUniqueID() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    @Override
    public IConditionProvider setIcon(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void readFromJSON(JsonObject data) {
        isVisible = JSONHelper.getBoolean(data, "isVisible", true);
        inverted = JSONHelper.getBoolean(data, "inverted", false);
    }

    @Override
    public void writeToJSON(JsonObject data) {
        JSONHelper.setBoolean(data, "isVisible", isVisible, true);
        JSONHelper.setBoolean(data, "inverted", inverted, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IConditionProvider)) return false;

        IConditionProvider that = (IConditionProvider) o;
        return getUniqueID() != null ? getUniqueID().equals(that.getUniqueID()) : that.getUniqueID() == null;

    }

    @Override
    public int hashCode() {
        return getUniqueID() != null ? getUniqueID().hashCode() : 0;
    }
}
