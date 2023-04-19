package de.thedead2.progression_reloaded.criteria.rewards;

import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeItem;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeLocation;
import de.thedead2.progression_reloaded.helpers.ItemHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.helpers.SpawnItemHelper;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.util.text.TextFormatting.DARK_GREEN;

@ProgressionRule(name="spawnItem", color=0xFFE599FF)
public class RewardSpawnItem extends RewardBaseItemFilter implements ICustomDescription, ICustomWidth, ICustomTooltip, IHasFilters, ISpecialFieldProvider, IRequestItem, ISpecialJSON {
    public List<IFilterProvider> locations = new ArrayList();
    public int stackSizeMin = 1;
    public int stackSizeMax = 1;

    protected transient IField field;

    public RewardSpawnItem() {
        field = new ItemFilterField("locations", this);
    }

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", stackSizeMin, stackSizeMax) + " \n" + field.getField();
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.DISPLAY ? 111: 100;
    }

    @Override
    public void addTooltip(List list) {
        list.add(DARK_GREEN + format(stackSizeMin, stackSizeMax));
        list.addAll(Arrays.asList(WordUtils.wrap((String)field.getField(), 28).split("\r\n")));
        ItemStack stack = getIcon();
        if (stack != null) {
            list.add("---");
            list.addAll(stack.getTooltip(MCClientHelper.getPlayer(), false));
        }
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) {
            fields.add(new ItemFilterField("locations", this));
            fields.add(new ItemFilterField("filters", this));
        } else fields.add(ProgressionAPI.fields.getItemPreview(this, "filters", 65, 42, 1.9F));
    }

    @Override
    public List<IFilterProvider> getAllFilters() {
        ArrayList<IFilterProvider> all = new ArrayList();
        all.addAll(locations);
        all.addAll(filters);
        return all;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        if (fieldName.equals("locations")) return FilterTypeLocation.INSTANCE;
        if (fieldName.equals("filters")) return FilterTypeItem.INSTANCE;

        return null;
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
                    for (int j = 0; j < 10; j++) {
                        WorldLocation location = WorldLocation.getRandomLocationFromFilters(locations, player);
                        if (location != null) {
                            BlockPos pos = new BlockPos(location.pos);
                            if (player.worldObj.isBlockLoaded(pos)) {
                                if (isValidLocation(player.worldObj, pos)) {
                                    SpawnItemHelper.spawnItem(player.worldObj, pos.getX(), pos.getY(), pos.getZ(), stack);
                                    return;
                                }
                            }
                        }
                    }
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

    //Helper Methods
    private boolean isValidLocation(World world, BlockPos pos) {
        IBlockState floorState = world.getBlockState(pos);
        IBlockState feetState = world.getBlockState(pos.up());
        IBlockState headState = world.getBlockState(pos.up(2));
        Material floor = floorState.getBlock().getMaterial(floorState);
        Material feet = feetState.getBlock().getMaterial(feetState);
        Material head = headState.getBlock().getMaterial(headState);
        if (feet.blocksMovement()) return false;
        if (head.blocksMovement()) return false;
        if (floor.isLiquid() || feet.isLiquid() || head.isLiquid()) return false;
        return floor.blocksMovement();
    }
}