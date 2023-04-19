package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.ProgressionAPI;
import de.thedead2.progression_reloaded.api.criteria.IField;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.*;
import de.thedead2.progression_reloaded.criteria.filters.block.FilterBaseBlock;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterFieldPreview;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeBlock;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeLocation;
import de.thedead2.progression_reloaded.helpers.ItemHelper;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

@ProgressionRule(name="placeBlock", color=0xFF00680A)
public class RewardPlaceBlock extends RewardBaseItemFilter implements ICustomDescription, ICustomWidth, ICustomTooltip, ISpecialFieldProvider, IRequestItem {
    public List<IFilterProvider> locations = new ArrayList();
    protected transient IField field;

    public RewardPlaceBlock() {
        field = new ItemFilterField("locations", this);
    }

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.translate("reward.placeBlock.description") + " \n" + field.getField();
    }

    @Override
    public int getWidth(DisplayMode mode) {
        return mode == DisplayMode.DISPLAY ? 116: 100;
    }

    @Override
    public void addTooltip(List list) {
        list.add(TextFormatting.BLUE + de.thedead2.progression_reloaded.ProgressionReloaded.translate("block.place"));
        list.add(getIcon().getDisplayName());
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        if (mode == DisplayMode.EDIT) {
            fields.add(new ItemFilterField("locations", this));
            fields.add(new ItemFilterFieldPreview("filters", this, 25, 40, 2F));
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
        if (fieldName.equals("filters")) return FilterTypeBlock.INSTANCE;

        return null;
    }

    @Override
    public boolean shouldRunOnce() {
        return true;
    }

    @Override
    public ItemStack getRequestedStack(EntityPlayer player) {
        return ItemHelper.getRandomItemOfSize(filters, player, 1);
    }

    @Override
    public void reward(EntityPlayer player, ItemStack stack) {
        if (stack != null) {
            for (IFilterProvider filter: filters) {
                if (filter.getProvided().matches(stack)) {
                    WorldLocation location = WorldLocation.getRandomLocationFromFilters(locations, player);
                    if (location != null) {
                        World world = DimensionManager.getWorld(location.dimension);
                        Block block = FilterBaseBlock.getBlock(stack);
                        int damage = stack.getItemDamage();
                        world.setBlockState(location.pos, block.getStateFromMeta(damage));
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void reward(EntityPlayerMP player) {
        ProgressionAPI.registry.requestItem(this, player);
    }
}
