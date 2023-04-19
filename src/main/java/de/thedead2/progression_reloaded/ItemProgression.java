package de.thedead2.progression_reloaded;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.crafting.Crafter;
import de.thedead2.progression_reloaded.crafting.CraftingRegistry;
import de.thedead2.progression_reloaded.crafting.CraftingUnclaimed;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.lib.GuiIDs;
import de.thedead2.progression_reloaded.network.PacketClaimed;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class ItemProgression extends Item {
    private static TIntObjectMap<ItemMeta> map;

    public static ItemStack getStackFromMeta(ItemMeta meta) {
        return new ItemStack(Progression.item, 1, meta.ordinal());
    }

    public static ItemMeta getMetaFromStack(ItemStack stack) {
        //If we haven't setup the data yet, let's do it now
        if (map == null) {
            map = new TIntObjectHashMap<ItemMeta>();
            for (ItemMeta meta: ItemMeta.values()) {
                map.put(meta.ordinal(), meta);
            }
        }

        return map.get(Math.max(0, Math.min(map.size() - 1, stack.getItemDamage())));
    }

    public enum ItemMeta {
        criteria, claim, book, edit, booleanValue, clearInventory, clearOrReceiveOrBlockCriteria, fallResistance,
        ifCriteriaCompleted, ifDayOrNight, ifHasAchievement, ifHasBoolean, ifHasPoints, ifIsAtCoordinates,
        ifIsBiome, ifRandom, onChangeDimension, onLogin, onReceivedAchiement, onReceivedBoolean,
        onReceivedPoints, onSecond, onSentMessage, points, speed, showTab, showLayer, sun, moon, stepAssist,
        attackPlayer, onGUIChange, eat, click, breaking, craft, kill, completed, openBook;
    }

    public static CreativeTabs tab;

    public ItemProgression() {
        final Item item = this;
        tab = new CreativeTabs("progression") {
            private ItemStack stack = new ItemStack(item, 1, ItemMeta.book.ordinal());

            @Override
            public String getTranslatedTabLabel() {
                return "Progression";
            }

            @Override
            public boolean hasSearchBar() {
                return true;
            }

            @Override
            public Item getTabIconItem() {
                return item;
            }

            @Override
            public int getIconItemDamage() {
                return ItemMeta.book.ordinal();
            }
        };

        setHasSubtypes(true);
        setMaxStackSize(1);
        setCreativeTab(tab);
    }

    public static ICriteria getCriteriaFromStack(ItemStack stack, boolean isClient) {
        if (!stack.hasTagCompound()) return null;
        if (stack.getItemDamage() != ItemMeta.criteria.ordinal()) return null;
        String uuid = stack.getTagCompound().getString("Criteria");
        if (uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            return APICache.getCache(isClient).getCriteria(UUID.fromString(uuid));
        } else return null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.getItemDamage() == ItemMeta.criteria.ordinal()) {
            ICriteria criteria = getCriteriaFromStack(stack, FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT);
            return criteria == null ? "BROKEN ITEM" : criteria.getLocalisedName();
        } else return Progression.translate("item." + getMetaFromStack(stack).name());
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (stack.getItemDamage() == ItemMeta.book.ordinal() || stack.getItemDamage() == ItemMeta.edit.ordinal()) {
            int guiid = player.isSneaking() ? GuiIDs.GROUP : GuiIDs.EDITOR;
            if (world.isRemote) {
                if (stack.getItemDamage() == ItemMeta.edit.ordinal()) MCClientHelper.FORCE_EDIT = true;
                else MCClientHelper.FORCE_EDIT = false;
            }

            player.openGui(Progression.instance, guiid, null, 0, 0, 0);
            return EnumActionResult.SUCCESS;
        }

        if (world.isRemote || player == null || stack == null) return EnumActionResult.PASS;
        if (stack.getItemDamage() == ItemMeta.claim.ordinal()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null) {
                Crafter crafter = CraftingRegistry.get(world.isRemote).getCrafterFromTile(tile);
                if (crafter == CraftingUnclaimed.INSTANCE) {
                    PlayerTracker.setTileOwner(tile, PlayerHelper.getUUIDForPlayer(player));
                    PacketHandler.sendToClient(new PacketClaimed(pos.getX(), pos.getY(), pos.getZ()), (EntityPlayerMP) player);
                    return EnumActionResult.SUCCESS;
                }
            }
        } else {
            ICriteria criteria = getCriteriaFromStack(stack, world.isRemote);
            if (criteria != null) {
                Result completed = PlayerTracker.getServerPlayer(PlayerHelper.getUUIDForPlayer(player)).getMappings().forceComplete(criteria);
                if (!player.capabilities.isCreativeMode && completed == Result.ALLOW) {
                    stack.stackSize--;
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (stack.getItemDamage() == ItemMeta.book.ordinal() || stack.getItemDamage() == ItemMeta.edit.ordinal()) {
            int guiid = player.isSneaking() ? GuiIDs.GROUP : GuiIDs.EDITOR;
            if (world.isRemote) {
                if (stack.getItemDamage() == ItemMeta.edit.ordinal()) MCClientHelper.FORCE_EDIT = true;
                else MCClientHelper.FORCE_EDIT = false;
            }

            player.openGui(Progression.instance, guiid, null, 0, 0, 0);
            return new ActionResult(EnumActionResult.SUCCESS, stack);
        } else if (!world.isRemote) {
            ICriteria criteria = getCriteriaFromStack(stack, world.isRemote);
            if (criteria != null) {
                Result completed = PlayerTracker.getServerPlayer(PlayerHelper.getUUIDForPlayer(player)).getMappings().forceComplete(criteria);
                if (!player.capabilities.isCreativeMode && completed == Result.ALLOW) {
                    stack.stackSize--;
                    return new ActionResult(EnumActionResult.SUCCESS, stack);
                }
            }
        }

        return new ActionResult(EnumActionResult.PASS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean debug) {
        if (stack.getItemDamage() == ItemMeta.claim.ordinal()) {
            list.add("Right click me on tiles");
            list.add("to claim them as yours");
        } else if (stack.getItemDamage() == ItemMeta.book.ordinal() || stack.getItemDamage() == ItemMeta.edit.ordinal()) {
            list.add(TextFormatting.ITALIC + "Hold Shift to Edit Team");
            if (player.capabilities.isCreativeMode || stack.getItemDamage() == ItemMeta.edit.ordinal()) {
                list.add("");
                list.add("Right click me to open");
                list.add("'Progression editor'");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        list.add(new ItemStack(item, 1, ItemMeta.book.ordinal()));
        list.add(new ItemStack(item, 1, ItemMeta.edit.ordinal()));
        list.add(new ItemStack(item, 1, ItemMeta.claim.ordinal()));

        if (APICache.getClientCache() != null) {
            for (ICriteria c : APICache.getClientCache().getCriteriaSet()) {
                ItemStack stack = new ItemStack(item);
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setString("Criteria", c.getUniqueID().toString());
                list.add(stack);
            }
        }
    }
}