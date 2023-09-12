package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import de.thedead2.progression_reloaded.data.abilities.restrictions.EntityRestriction;
import de.thedead2.progression_reloaded.data.level.TestLevels;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class EntityRestrictionManager extends RestrictionManager<EntityRestriction, EntityType<?>> {

    public EntityRestrictionManager() {
        super(new ResourceLocation(ModHelper.MOD_ID, "entity_restriction_manager"), () -> DefaultAction.DENY);
        this.addRestriction(EntityTypeTags.SKELETONS, new EntityRestriction(TestLevels.TEST2.getId(), RestrictionKey.wrap(EntityTypeTags.SKELETONS),
                                                                            null, false, 256, true, null
        ));
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
        return "EntityRestrictionManager";
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(final LivingSpawnEvent.CheckSpawn event) {
        Entity entity = event.getEntity();
        Pair<Boolean, EntityRestriction> pair1 = isRestricted(entity.getType());
        if(pair1.getLeft()) {
            EntityRestriction restriction = pair1.getRight();

            if(restriction.allowSpawns() || (event.isSpawner() && restriction.allowSpawners())) {
                return;
            }

            var pair = LevelManager.getInstance().getHighestPlayerLevel(event.getLevel().players());

            Player player = pair.getLeft();
            if(entity.distanceTo(player) <= restriction.getDistanceToPlayer() && this.meetsRequirements(entity, player, restriction)) {
                event.setResult(Event.Result.DENY);
                ModHelper.LOGGER.debug("Hindered entity {} to spawn at pos {}, {}, {} as it is restricted for player {} in dimension {}", entity.getType(), event.getX(), event.getY(), event.getZ(), player.getDisplayName()
                                                                                                                                                                                                            .getString(), entity.level.dimension()
                                                                                                                                                                                                                                      .location());
                if(restriction.getEntityReplacement() != null) {
                    EntityType<?> replacement = ForgeRegistries.ENTITY_TYPES.getValue(restriction.getEntityReplacement());
                    replacement.spawn((ServerLevel) entity.getLevel(), new BlockPos(event.getX(), event.getY(), event.getZ()), event.getSpawnReason());
                }
            }
        }
    }


    @Override
    public ImmutablePair<Boolean, EntityRestriction> isRestricted(EntityType<?> entityType) {
        ResourceLocation entityId = EntityType.getKey(entityType);
        var pair1 = isRestrictedById(entityId);
        return pair1.getLeft() ? pair1 : isRestrictedByTag(entityType.getTags());
    }


    private boolean meetsRequirements(Entity entity, Player player, EntityRestriction restriction) {
        ResourceLocation dimensionId = entity.level.dimension().location();
        return restriction.getDimension() == null ? this.doesNotHaveLevel(player, restriction) : dimensionId.equals(restriction.getDimension()) && this.doesNotHaveLevel(player, restriction);
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityAttack(LivingAttackEvent event) {
        Entity entity = event.getEntity();
        DamageSource damageSource = event.getSource();
        Pair<Boolean, EntityRestriction> pair = isRestricted(entity.getType());
        if(pair.getLeft() && damageSource.getEntity() instanceof Player player) {
            if(this.meetsRequirements(entity, player, pair.getRight())) {
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteraction(PlayerInteractEvent.EntityInteract event) {
        Entity entity = event.getTarget();
        Pair<Boolean, EntityRestriction> pair = isRestricted(entity.getType());
        if(pair.getLeft()) {
            if(this.meetsRequirements(entity, event.getEntity(), pair.getRight())) {
                event.setCanceled(true);
            }
        }
    }
}
