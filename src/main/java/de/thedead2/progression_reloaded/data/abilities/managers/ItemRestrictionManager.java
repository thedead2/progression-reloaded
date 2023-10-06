package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import de.thedead2.progression_reloaded.data.abilities.restrictions.ItemRestriction;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class ItemRestrictionManager extends RestrictionManager<ItemRestriction, Item> {


    public ItemRestrictionManager() {
        super(new ResourceLocation(MOD_ID, "item_restriction_manager"), () -> DefaultAction.DENY);
        this.addRestriction(ItemTags.LEAVES, new ItemRestriction(TestLevels.TEST2.getId(), RestrictionKey.wrap(ItemTags.LEAVES)));
        this.addRestriction(MOD_ID, new ItemRestriction(TestLevels.TEST2.getId(), RestrictionKey.wrap(MOD_ID)));
    }


    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return null;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {

    }


    @Override
    public @NotNull String getName() {
        return "ItemRestrictionManager";
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemPickUp(final EntityItemPickupEvent event) {
        Item item = event.getItem().getItem().getItem();
        Pair<Boolean, ItemRestriction> restrictionPair = isRestricted(item);
        if(restrictionPair.getLeft()) {
            ItemRestriction restriction = restrictionPair.getRight();
            if(!restriction.isAllowedToBePickedUp() && this.doesNotHaveLevel(event.getEntity(), restriction)) {
                event.setCanceled(true);
            }
        }
    }


    @Override
    public ImmutablePair<Boolean, ItemRestriction> isRestricted(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        var pair1 = isRestrictedById(itemId);
        return pair1.getLeft() ? pair1 : isRestrictedByTag(item.builtInRegistryHolder().getTagKeys());
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST) //TODO: tick only on server!
    public void onInventoryTick(final TickEvent.PlayerTickEvent event) {
        if(event.side.isClient()) {
            return;
        }
        Player player = event.player;
        final Inventory inventory = player.getInventory();

        final int armorStart = inventory.items.size();
        final int armorEnd = armorStart + inventory.armor.size();

        for(int slot = 0; slot < inventory.getContainerSize(); slot++) {

            final ItemStack slotContent = inventory.getItem(slot);

            Pair<Boolean, ItemRestriction> restrictionPair = isRestricted(slotContent.getItem());
            if(!slotContent.isEmpty() && restrictionPair.getLeft()) {
                ItemRestriction restriction = restrictionPair.getRight();
                if(slot >= armorStart && slot <= armorEnd) {
                    if(!restriction.isAllowedEquipped() && this.doesNotHaveLevel(player, restriction)) {
                        inventory.setItem(slot, ItemStack.EMPTY);
                        player.drop(slotContent, false);
                    }
                }
                else {
                    if(!restriction.isAllowedInInventory() && this.doesNotHaveLevel(player, restriction)) {
                        inventory.setItem(slot, ItemStack.EMPTY);
                        player.drop(slotContent, false);
                    }
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUse(final LivingEntityUseItemEvent.Start event) {
        Item item = event.getItem().getItem();
        Pair<Boolean, ItemRestriction> restrictionPair = isRestricted(item);
        if(event.getEntity() instanceof ServerPlayer player && restrictionPair.getLeft()) {
            ItemRestriction restriction = restrictionPair.getRight();
            if(!restriction.isAllowedToBeUsed() && this.doesNotHaveLevel(player, restriction)) {
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAttackWithItem(final LivingAttackEvent event) {
        if(event.getSource().getEntity() instanceof Player player) {
            Item item = player.getMainHandItem().getItem();
            Pair<Boolean, ItemRestriction> restrictionPair = isRestricted(item);
            if(restrictionPair.getLeft()) {
                ItemRestriction restriction = restrictionPair.getRight();
                if(!restriction.isAllowedForAttacking() && this.doesNotHaveLevel(player, restriction)) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
