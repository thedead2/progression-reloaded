package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.api.INbtSerializable;
import de.thedead2.progression_reloaded.api.network.INetworkSerializable;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;


public class PlayerLevels implements INetworkSerializable, INbtSerializable {

    private final Supplier<PlayerData> playerData;

    private final Map<ProgressionLevel, LevelProgress> levelProgress = Maps.newHashMap();

    private ProgressionLevel currentLevel;

    @Nullable
    private ProgressionLevel cachedLevel;


    public PlayerLevels(Supplier<PlayerData> playerData) {
        this.playerData = playerData;
        this.currentLevel = playerData.get()
                                      .getPlayer() instanceof ServerPlayer serverPlayer ? serverPlayer.gameMode.isCreative() && ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get() ? LevelManager.CREATIVE : ProgressionLevel.fromKey(ResourceLocationHelper.getOrDefault(ConfigManager.DEFAULT_STARTING_LEVEL.get(), LevelManager.CREATIVE.getId())) : LevelManager.CREATIVE;

        ModRegistries.LEVELS.get().getValues().forEach(level -> this.levelProgress.put(level, new LevelProgress(this.playerData, level)));
    }


    private PlayerLevels(Supplier<PlayerData> playerData, ProgressionLevel level, @Nullable ProgressionLevel cachedLevel, Map<ProgressionLevel, LevelProgress> levelProgress) {
        this.playerData = playerData;
        this.currentLevel = level;
        this.cachedLevel = cachedLevel;
        this.levelProgress.putAll(levelProgress);
    }


    public static PlayerLevels fromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        ProgressionLevel currentLevel = ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag.getString("currentLevel")));
        ProgressionLevel cachedLevel = SerializationHelper.getNullable(tag, "cachedLevel", tag1 -> ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag1.getAsString())));
        Map<ProgressionLevel, LevelProgress> levelProgress = CollectionHelper.loadFromNBT(tag.getCompound("levelProgress"), s -> ModRegistries.LEVELS.get().getValue(new ResourceLocation(s)), tag1 -> LevelProgress.fromNBT((CompoundTag) tag1));

        return new PlayerLevels(() -> PlayerDataManager.getPlayerData(uuid), currentLevel, cachedLevel, levelProgress);
    }


    public static PlayerLevels fromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ProgressionLevel level = ModRegistries.LEVELS.get().getValue(buf.readResourceLocation());
        ProgressionLevel cachedLevel = buf.readNullable(buf1 -> ModRegistries.LEVELS.get().getValue(buf1.readResourceLocation()));
        Map<ProgressionLevel, LevelProgress> levelProgress = buf.readMap(buf1 -> ModRegistries.LEVELS.get().getValue(buf1.readResourceLocation()), LevelProgress::fromNetwork);

        return new PlayerLevels(() -> PlayerDataManager.getPlayerData(uuid), level, cachedLevel, levelProgress);
    }


    @Override
    public @NotNull CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", this.playerData.get().getUUID());
        tag.putString("currentLevel", this.currentLevel.getId().toString());
        SerializationHelper.addNullable(this.cachedLevel, tag, "cachedLevel", level -> StringTag.valueOf(level.getId().toString()));
        tag.put("levelProgress", CollectionHelper.saveToNBT(this.levelProgress, level -> level.getId().toString(), LevelProgress::toNBT));

        return tag;
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerData.get().getUUID());
        buf.writeResourceLocation(this.currentLevel.getId());
        buf.writeNullable(this.cachedLevel, (buf1, level) -> buf1.writeResourceLocation(level.getId()));
        buf.writeMap(this.levelProgress, (buf1, level) -> buf1.writeResourceLocation(level.getId()), (buf1, progress) -> progress.toNetwork(buf1));
    }


    public ProgressionLevel getCurrentLevel() {
        return currentLevel;
    }


    public void updateAndCacheLevel(ProgressionLevel level) {
        this.cachedLevel = this.currentLevel;
        this.updateLevel(level);
    }


    public void updateLevel(ProgressionLevel level) {
        ProgressionLevel previousLevel = this.currentLevel;
        this.currentLevel = level;
        PREventFactory.onLevelChanged(this.currentLevel, this.playerData.get(), previousLevel);
    }


    public void restoreCachedLevel() {
        if(this.cachedLevel != null) {
            this.updateLevel(this.cachedLevel);
        }
        else {
            this.updateLevel(ModRegistries.LEVELS.get().getValue(new ResourceLocation(ConfigManager.DEFAULT_STARTING_LEVEL.get())));
        }
    }


    public void resetLevelProgress(ProgressionLevel level) {
        this.levelProgress.get(level).reset();
    }


    public void completeLevelProgress(ProgressionLevel level) {
        this.levelProgress.get(level).complete();
    }


    public LevelProgress getCurrentLevelProgress() {
        return this.levelProgress.get(this.currentLevel);
    }
}
