package de.thedead2.progression_reloaded.data.abilities.managers;

import com.google.common.collect.ImmutableMap;
import de.thedead2.progression_reloaded.data.abilities.DefaultAction;
import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import de.thedead2.progression_reloaded.data.abilities.restrictions.Restriction;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientSyncRestrictionsPacket;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class RestrictionManager<T extends Restriction<R>, R> extends SimpleJsonReloadListener {

    protected final ResourceLocation id;

    private final Map<RestrictionKey<R>, T> restrictions = new HashMap<>();

    private final Map<RestrictionKey<R>, T> orgRestrictions = new HashMap<>();

    protected Supplier<DefaultAction> defaultAction;

    private Supplier<DefaultAction> orgDefaultAction;


    protected RestrictionManager(ResourceLocation id, Supplier<DefaultAction> defaultAction) {
        this.id = id;
        this.defaultAction = defaultAction;
    }


    /**
     * Should check the given object against {@link #isRestrictedById(ResourceLocation)} and {@link #isRestrictedByTag(TagKey)}
     * for the given object.
     *
     * @param object The object to check for restriction.
     *
     * @return Returns a {@link ImmutablePair} of a boolean value and a {@link Restriction}.
     * <P>The boolean value defines whether the object is restricted or not, not null</P>
     * <P>The Restriction is the corresponding restriction for the object, null if the boolean value is false</P>
     **/
    public abstract ImmutablePair<Boolean, T> isRestricted(R object);


    protected ImmutablePair<Boolean, T> isRestrictedByTag(Stream<TagKey<R>> tags) {
        for(TagKey<R> tag : tags.collect(Collectors.toSet())) {
            ImmutablePair<Boolean, T> pair = isRestrictedByTag(tag);
            if(pair.getLeft()) {
                return pair;
            }
        }
        return ImmutablePair.of(false, null);
    }


    protected ImmutablePair<Boolean, T> isRestrictedByTag(TagKey<R> tag) {
        RestrictionKey<R> restrictionKey = RestrictionKey.wrap(tag);
        if(this.restrictions.containsKey(restrictionKey)) {
            return ImmutablePair.of(true, this.restrictions.get(restrictionKey));
        }

        return ImmutablePair.of(false, null);
    }


    protected ImmutablePair<Boolean, T> isRestrictedById(ResourceLocation id) {
        RestrictionKey<R> restrictionKey = RestrictionKey.wrap(id);
        if(this.restrictions.containsKey(restrictionKey)) {
            return ImmutablePair.of(true, this.restrictions.get(restrictionKey));
        }
        restrictionKey = RestrictionKey.wrap(id.getNamespace());
        if(this.restrictions.containsKey(restrictionKey)) {
            return ImmutablePair.of(true, this.restrictions.get(restrictionKey));
        }
        return ImmutablePair.of(false, null);
    }


    public void addRestriction(ResourceLocation id, T restriction) {
        this.addRestriction(RestrictionKey.wrap(id), restriction);
    }


    public void addRestriction(RestrictionKey<R> key, T restriction) {
        this.restrictions.put(key, restriction);
    }


    public void addRestriction(String modId, T restriction) {
        this.addRestriction(RestrictionKey.wrap(modId), restriction);
    }


    public void addRestriction(TagKey<R> tag, T restriction) {
        this.addRestriction(RestrictionKey.wrap(tag), restriction);
    }


    @SuppressWarnings("unchecked")
    public void syncRestrictionsWithClient(ServerPlayer serverPlayer, boolean shouldReset) {
        if(ModHelper.isRunningOnServerThread()) {
            ModNetworkHandler.sendToPlayer(
                    new ClientSyncRestrictionsPacket(this.id, shouldReset, this.defaultAction.get(), CollectionHelper.convertMap(this.restrictions, new HashMap<>(), key -> (RestrictionKey<Object>) key, t -> (Restriction<Object>) t)),
                    serverPlayer
            );
        }
    }


    @OnlyIn(Dist.CLIENT)
    public void syncRestrictions(Map<RestrictionKey<R>, T> restrictions, DefaultAction defaultAction) {
        this.orgRestrictions.putAll(this.restrictions);
        this.orgDefaultAction = this.defaultAction;

        this.restrictions.clear();
        this.defaultAction = null;
        this.restrictions.putAll(restrictions);
        this.defaultAction = () -> defaultAction;
    }


    public void reset() {
        this.restrictions.clear();
        this.defaultAction = null;
        this.restrictions.putAll(this.orgRestrictions);
        this.defaultAction = this.orgDefaultAction;
    }


    @Nullable
    public T getRestriction(ResourceLocation id) {
        return this.getRestriction(RestrictionKey.wrap(id));
    }


    @Nullable
    public T getRestriction(RestrictionKey<R> key) {
        return this.restrictions.get(key);
    }


    @Nullable
    public T getRestriction(String modId) {
        return this.getRestriction(RestrictionKey.wrap(modId));
    }


    @Nullable
    public T getRestriction(TagKey<R> tag) {
        return this.getRestriction(RestrictionKey.wrap(tag));
    }


    public ImmutableMap<RestrictionKey<R>, T> getRestrictions() {
        return ImmutableMap.copyOf(this.restrictions);
    }


    public boolean doesNotHaveLevel(Player player, T restriction) {
        ProgressionLevel level = PlayerDataHandler.getActivePlayer(player).getProgressionLevel();
        return !level.contains(ModRegistries.LEVELS.get().getValue(restriction.getLevel()));
    }
}
