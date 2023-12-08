package de.thedead2.progression_reloaded.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.api.IRestrictionType;
import de.thedead2.progression_reloaded.data.restrictions.*;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncRestrictionsPacket;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.thedead2.progression_reloaded.data.restrictions.RestrictionTypes.*;


public class RestrictionManager {

    private static final Marker MARKER = new MarkerManager.Log4jMarker("RestrictionManager");
    public static final BiMap<ResourceLocation, IRestrictionType<?>> RESTRICTION_TYPES = HashBiMap.create();

    private final Map<IRestrictionType<?>, Map<RestrictionKey<?>, Restriction<?>>> restrictions = Maps.newHashMap();


    public RestrictionManager() {
        //this.addRestriction(DIMENSION, Level.NETHER, new DimensionRestriction(TestLevels.TEST2.getId(), Component.literal("You are not allowed to enter the nether yet!")));
        //this.addRestriction(ENTITY, EntityType.SKELETON, new EntityRestriction(TestLevels.TEST2.getId(), null, false, 256, false, null));
        //this.addRestriction(ITEM, Items.DIAMOND, new ItemRestriction(TestLevels.TEST2.getId()));
    }


    public <T> void addRestriction(IRestrictionType<T> type, T restrictedObj, Restriction<T> restriction) {
        this.addRestriction(type, RestrictionKey.wrap(type.get(restrictedObj).getKey()), restriction);
    }


    private <T> void addRestriction(IRestrictionType<T> type, RestrictionKey<T> key, Restriction<T> restriction) {
        if(!RESTRICTION_TYPES.containsValue(type)) {
            throw new IllegalArgumentException("Tried to add restriction for unknown type: " + type.toString());
        }
        this.restrictions.computeIfAbsent(type, type1 -> new HashMap<>()).put(key, restriction);
    }


    public <T> void addRestriction(IRestrictionType<T> type, String modId, Restriction<T> restriction) {
        this.addRestriction(type, RestrictionKey.wrap(modId), restriction);
    }


    public <T> void addRestriction(IRestrictionType<T> type, TagKey<T> tag, Restriction<T> restriction) {
        this.addRestriction(type, RestrictionKey.wrap(tag), restriction);
    }


    public <T> Restriction<T> removeRestriction(IRestrictionType<T> type, RestrictionKey<T> key) {
        Map<RestrictionKey<T>, Restriction<T>> restrictions = this.getRestrictionsForType(type);
        return restrictions.remove(key);
    }


    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Map<RestrictionKey<T>, Restriction<T>> getRestrictionsForType(IRestrictionType<T> type) {
        Map<RestrictionKey<?>, Restriction<?>> restrictions = this.restrictions.get(type);
        if(restrictions != null) {
            return CollectionHelper.convertMap(restrictions, restrictionKey -> (RestrictionKey<T>) restrictionKey, restriction -> (Restriction<T>) restriction);
        }
        return Maps.newHashMap();
    }


    public void acceptSync(Map<IRestrictionType<?>, Map<RestrictionKey<?>, Restriction<?>>> restrictions) {
        this.restrictions.clear();
        this.restrictions.putAll(restrictions);
    }


    public void syncRestrictions(ServerPlayer player) {
        if(ModHelper.isRunningOnServerThread()) {
            ModNetworkHandler.sendToPlayer(new ClientSyncRestrictionsPacket(this.restrictions), player);
        }
    }


    public <T> boolean isRestricted(IRestrictionType<T> type, T object) {
        Map<RestrictionKey<T>, Restriction<T>> restrictions = this.getRestrictionsForType(type);
        Pair<ResourceLocation, Stream<TagKey<T>>> ids = type.get(object);

        return this.isRestrictedById(ids.getLeft(), restrictions) || this.isRestrictedByTag(ids.getRight(), restrictions);
    }


    protected <T> boolean isRestrictedById(ResourceLocation id, Map<RestrictionKey<T>, Restriction<T>> restrictions) {
        RestrictionKey<T> restrictionKey = RestrictionKey.wrap(id);
        if(restrictions.containsKey(restrictionKey)) {
            return true;
        }
        else {
            RestrictionKey<T> restrictionKey1 = RestrictionKey.wrap(id.getNamespace());
            return restrictions.containsKey(restrictionKey1);
        }
    }


    protected <T> boolean isRestrictedByTag(Stream<TagKey<T>> tags, Map<RestrictionKey<T>, Restriction<T>> restrictions) {
        boolean bool = false;
        for(TagKey<T> tag : tags.collect(Collectors.toSet())) {
            RestrictionKey<T> restrictionKey = RestrictionKey.wrap(tag);
            bool = restrictions.containsKey(restrictionKey);

            if(bool) {
                break;
            }
        }
        return bool;
    }


    @Nullable
    public <T, R extends Restriction<T>> R getRestrictionFor(IRestrictionType<T> type, Class<R> restrictionClass, T object) {
        Map<RestrictionKey<T>, Restriction<T>> restrictions = this.getRestrictionsForType(type);
        Pair<ResourceLocation, Stream<TagKey<T>>> ids = type.get(object);

        R restriction = this.getRestrictionById(ids.getLeft(), restrictionClass, restrictions);
        return restriction != null ? restriction : this.getRestrictionByTag(ids.getRight(), restrictionClass, restrictions);
    }


    @Nullable
    protected <T, R extends Restriction<T>> R getRestrictionById(ResourceLocation id, Class<R> restrictionClass, Map<RestrictionKey<T>, Restriction<T>> restrictions) {
        RestrictionKey<T> restrictionKey = RestrictionKey.wrap(id);
        Restriction<T> restriction = restrictions.get(restrictionKey);
        if(restriction != null) {
            return restrictionClass.cast(restriction);
        }
        else {
            RestrictionKey<T> restrictionKey1 = RestrictionKey.wrap(id.getNamespace());
            return restrictionClass.cast(restrictions.get(restrictionKey1));
        }
    }


    @Nullable
    protected <T, R extends Restriction<T>> R getRestrictionByTag(Stream<TagKey<T>> tags, Class<R> restrictionClass, Map<RestrictionKey<T>, Restriction<T>> restrictions) {
        Restriction<T> restriction = null;
        for(TagKey<T> tag : tags.collect(Collectors.toSet())) {
            RestrictionKey<T> restrictionKey = RestrictionKey.wrap(tag);
            restriction = restrictions.get(restrictionKey);

            if(restriction != null) {
                break;
            }
        }
        return restrictionClass.cast(restriction);
    }


    @Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Listeners {

        private static final Supplier<RestrictionManager> restrictionManager = () -> LevelManager.getInstance().getRestrictionManager();

        //<---------------------------------------- Items ---------------------------------------->


        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onItemPickUp(final EntityItemPickupEvent event) {
            Item item = event.getItem().getItem().getItem();
            if(restrictionManager.get().isRestricted(ITEM, item)) {
                ItemRestriction restriction = restrictionManager.get().getRestrictionFor(ITEM, ItemRestriction.class, item);
                if(!restriction.isAllowedToBePickedUp() && restriction.isActiveForPlayer(event.getEntity())) {
                    event.setCanceled(true);
                }
            }
        }


        @SubscribeEvent(priority = EventPriority.HIGHEST) //TODO: tick only on server!
        public static void onInventoryTick(final TickEvent.PlayerTickEvent event) {
            if(event.side.isClient()) {
                return;
            }
            Player player = event.player;
            final Inventory inventory = player.getInventory();

            final int armorStart = inventory.items.size();
            final int armorEnd = armorStart + inventory.armor.size();

            for(int slot = 0; slot < inventory.getContainerSize(); slot++) {

                final ItemStack slotContent = inventory.getItem(slot);

                if(!slotContent.isEmpty() && restrictionManager.get().isRestricted(ITEM, slotContent.getItem())) {
                    ItemRestriction restriction = restrictionManager.get().getRestrictionFor(ITEM, ItemRestriction.class, slotContent.getItem());
                    if(slot >= armorStart && slot <= armorEnd) {
                        if(!restriction.isAllowedEquipped() && restriction.isActiveForPlayer(player)) {
                            inventory.setItem(slot, ItemStack.EMPTY);
                            player.drop(slotContent, false);
                        }
                    }
                    else {
                        if(!restriction.isAllowedInInventory() && restriction.isActiveForPlayer(player)) {
                            inventory.setItem(slot, ItemStack.EMPTY);
                            player.drop(slotContent, false);
                        }
                    }
                }
            }
        }


        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onItemUse(final LivingEntityUseItemEvent.Start event) {
            Item item = event.getItem().getItem();
            if(event.getEntity() instanceof ServerPlayer player && restrictionManager.get().isRestricted(ITEM, item)) {
                ItemRestriction restriction = restrictionManager.get().getRestrictionFor(ITEM, ItemRestriction.class, item);
                if(!restriction.isAllowedToBeUsed() && restriction.isActiveForPlayer(player)) {
                    event.setCanceled(true);
                }
            }
        }


        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onAttackWithItem(final LivingAttackEvent event) {
            if(event.getSource().getEntity() instanceof Player player) {
                Item item = player.getMainHandItem().getItem();

                if(restrictionManager.get().isRestricted(ITEM, item)) {
                    ItemRestriction restriction = restrictionManager.get().getRestrictionFor(ITEM, ItemRestriction.class, item);
                    if(!restriction.isAllowedForAttacking() && restriction.isActiveForPlayer(player)) {
                        event.setCanceled(true);
                    }
                }
            }
        }


        //<---------------------------------------- Blocks ---------------------------------------->


        @SubscribeEvent
        public static void onBlockBreak(final BlockEvent.BreakEvent event) {
            LevelAccessor level = event.getLevel();
            Player player = event.getPlayer();
            BlockState blockToBreak = event.getState();
            BlockPos blockPos = event.getPos();

            if(restrictionManager.get().isRestricted(BLOCK, blockToBreak.getBlock())) {
                BlockRestriction restriction = restrictionManager.get().getRestrictionFor(BLOCK, BlockRestriction.class, blockToBreak.getBlock());

            }
        }


        //<---------------------------------------- Entities ---------------------------------------->


        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntitySpawn(final LivingSpawnEvent.CheckSpawn event) {
            Entity entity = event.getEntity();

            if(restrictionManager.get().isRestricted(ENTITY, entity.getType())) {
                EntityRestriction restriction = restrictionManager.get().getRestrictionFor(ENTITY, EntityRestriction.class, entity.getType());

                if(restriction.allowSpawns() || (event.isSpawner() && restriction.allowSpawners())) {
                    return;
                }

                var pair = LevelManager.getInstance().getHighestPlayerLevel(event.getLevel().players());

                Player player = pair.getLeft();
                if(entity.distanceTo(player) <= restriction.getDistanceToPlayer() && meetsRequirements(entity, player, restriction)) {
                    event.setResult(Event.Result.DENY);
                    ModHelper.LOGGER.debug(MARKER, "Hindered entity {} to spawn at setPoint {}, {}, {} as it is restricted for player {} in dimension {}", entity.getType(), event.getX(), event.getY(), event.getZ(), player.getDisplayName()
                                                                                                                                                                                                                     .getString(), entity.level.dimension()
                                                                                                                                                                                                                                               .location());
                    if(restriction.getEntityReplacement() != null) {
                        EntityType<?> replacement = ForgeRegistries.ENTITY_TYPES.getValue(restriction.getEntityReplacement());
                        replacement.spawn((ServerLevel) entity.getLevel(), new BlockPos(event.getX(), event.getY(), event.getZ()), event.getSpawnReason());
                    }
                }
            }
        }


        private static boolean meetsRequirements(Entity entity, Player player, EntityRestriction restriction) {
            ResourceLocation dimensionId = entity.level.dimension().location();
            return restriction.getDimension() == null ? restriction.isActiveForPlayer(player) : dimensionId.equals(restriction.getDimension()) && restriction.isActiveForPlayer(player);
        }


        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntityAttack(LivingAttackEvent event) {
            Entity entity = event.getEntity();
            DamageSource damageSource = event.getSource();

            if(restrictionManager.get().isRestricted(ENTITY, entity.getType()) && damageSource.getEntity() instanceof Player player) {
                if(meetsRequirements(entity, player, restrictionManager.get().getRestrictionFor(ENTITY, EntityRestriction.class, entity.getType()))) {
                    event.setCanceled(true);
                }
            }
        }


        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntityInteraction(PlayerInteractEvent.EntityInteract event) {
            Entity entity = event.getTarget();
            if(restrictionManager.get().isRestricted(ENTITY, entity.getType())) {
                if(meetsRequirements(entity, event.getEntity(), restrictionManager.get().getRestrictionFor(ENTITY, EntityRestriction.class, entity.getType()))) {
                    event.setCanceled(true);
                }
            }
        }


        //<---------------------------------------- Dimensions ---------------------------------------->


        @SubscribeEvent
        public static void onDimensionChange(EntityTravelToDimensionEvent event) {
            Entity entity = event.getEntity();
            if(entity instanceof ServerPlayer player && restrictionManager.get().isRestricted(DIMENSION, event.getDimension())) {
                DimensionRestriction restriction = restrictionManager.get().getRestrictionFor(DIMENSION, DimensionRestriction.class, event.getDimension());
                if(restriction.isActiveForPlayer(player)) {
                    event.setCanceled(true);
                    if(restriction.getRestrictionMessage() != null) {
                        player.sendSystemMessage(restriction.getRestrictionMessage());
                    }
                }
            }
        }
    }
}
