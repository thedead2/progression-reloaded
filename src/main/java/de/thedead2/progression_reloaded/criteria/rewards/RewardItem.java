package de.thedead2.progression_reloaded.criteria.rewards;

import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterFieldPreview;
import de.thedead2.progression_reloaded.helpers.ItemHelper;
import de.thedead2.progression_reloaded.helpers.SpawnItemHelper;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketRewardItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Random;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;
import static de.thedead2.progression_reloaded.gui.core.GuiList.MODE;
import static net.minecraft.util.text.TextFormatting.BLUE;

@ProgressionRule(name="item", color=0xFFE599FF)
public class RewardItem extends RewardBaseItemFilter implements ICustomDisplayName, ICustomDescription, ICustomWidth, ICustomTooltip, ISpecialFieldProvider, IStackSizeable, IRequestItem, ISpecialJSON {
    private static final Random rand = new Random();
    public int stackSizeMin = 1;
    public int stackSizeMax = 1;

    @Override
    public String getDisplayName() {
        return MODE == EDIT ? de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName()) : de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName() + ".display");
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == EDIT ? 100 : 55;
    }

    @Override
    public void addTooltip(List list) {
        list.add(BLUE + de.thedead2.progression_reloaded.ProgressionReloaded.translate("item.free"));
        list.add(getIcon().getDisplayName() + " x" + stackSizeMin + " to " + stackSizeMax);
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == EDIT) fields.add(new ItemFilterFieldPreview("filters", this, 50, 40, 2F));
        else fields.add(new ItemFilterFieldPreview("filters", this, 5, 25, 2.8F));
    }

    @Override
    public int getStackSize() {
        int random = Math.max(0, (stackSizeMax - stackSizeMin));
        int additional = 0;
        if (random != 0) {
            additional = rand.nextInt(random + 1);
        }

        return stackSizeMin + additional;
    }

    @Override
    public ItemStack getRequestedStack(EntityPlayer player) {
        int random = Math.max(0, (stackSizeMax - stackSizeMin));
        int additional = 0;
        if (random != 0) {
            additional = player.worldObj.rand.nextInt(random + 1);
        }

        int amount = stackSizeMin + additional;
        return ItemHelper.getRandomItemOfSize(filters, player, amount);
    }

    @Override
    public void reward(EntityPlayer player, ItemStack stack) {
        if (stack != null) {
            for (IFilterProvider filter: filters) {
                if (filter.getProvided().matches(stack)) {
                    PacketHandler.sendToClient(new PacketRewardItem(stack.copy()), (EntityPlayerMP) player);
                    SpawnItemHelper.addToPlayerInventory(player, stack.copy());
                    return;
                }
            }
        }
    }

    @Override
    public void reward(EntityPlayerMP player) {
        ProgressionAPI.registry.requestItem(this, player);
    }

    @Override
    public boolean onlySpecial() {
        return false;
    }

    @Override
    public void readFromJSON(JsonObject data) {
        if (data.get("stackSize") != null) {
            stackSizeMin = data.get("stackSize").getAsInt();
            stackSizeMax = data.get("stackSize").getAsInt();
        }
    }

    @Override
    public void writeToJSON(JsonObject object) {}
}