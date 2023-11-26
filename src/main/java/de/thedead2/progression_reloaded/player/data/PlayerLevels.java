package de.thedead2.progression_reloaded.player.data;

import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.level.LevelProgress;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;


public class PlayerLevels {

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


    public static PlayerLevels loadFromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("player");
        ProgressionLevel currentLevel = ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag.getString("currentLevel")));
        ProgressionLevel cachedLevel = ModRegistries.LEVELS.get().getValue(new ResourceLocation(tag.getString("cachedLevel")));
        Map<ProgressionLevel, LevelProgress> levelProgress = CollectionHelper.loadFromNBT(tag.getCompound("levelProgress"), s -> ModRegistries.LEVELS.get().getValue(new ResourceLocation(s)), tag1 -> LevelProgress.loadFromCompoundTag((CompoundTag) tag1));

        return new PlayerLevels(() -> PlayerDataManager.getPlayerData(uuid), currentLevel, cachedLevel, levelProgress);
    }


    public static PlayerLevels loadFromNetwork(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ProgressionLevel level = ModRegistries.LEVELS.get().getValue(buf.readResourceLocation());
        ProgressionLevel cachedLevel = buf.readNullable(buf1 -> ModRegistries.LEVELS.get().getValue(buf1.readResourceLocation()));
        Map<ProgressionLevel, LevelProgress> levelProgress = buf.readMap(buf1 -> ModRegistries.LEVELS.get().getValue(buf1.readResourceLocation()), LevelProgress::fromNetwork);

        return new PlayerLevels(() -> PlayerDataManager.getPlayerData(uuid), level, cachedLevel, levelProgress);
    }


    public @NotNull CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("player", this.playerData.get().getUUID());
        tag.putString("currentLevel", this.currentLevel.getId().toString());
        tag.putString("cachedLevel", this.cachedLevel != null ? this.cachedLevel.getId().toString() : "null");
        tag.put("levelProgress", CollectionHelper.saveToNBT(this.levelProgress, level -> level.getId().toString(), LevelProgress::saveToCompoundTag));

        return tag;
    }


    public void saveToNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerData.get().getUUID());
        buf.writeResourceLocation(this.currentLevel.getId());
        buf.writeNullable(this.cachedLevel, (buf1, level) -> buf1.writeUtf(level.getId().toString()));
        buf.writeMap(this.levelProgress, (buf1, level) -> buf1.writeResourceLocation(level.getId()), (buf1, progress) -> progress.toNetwork(buf1));
    }


    public ProgressionLevel getCurrentLevel() {
        return currentLevel;
    }


    public void updateAndCacheLevel(ProgressionLevel level) {
        this.cachedLevel = this.currentLevel;
        this.currentLevel = level;
    }


    public void updateLevel(ProgressionLevel level) {
        this.currentLevel = level;
    }


    public void restoreCachedLevel() {
        if(this.cachedLevel != null) {
            this.currentLevel = this.cachedLevel;
        }
        else {
            this.currentLevel = ModRegistries.LEVELS.get().getValue(new ResourceLocation(ConfigManager.DEFAULT_STARTING_LEVEL.get()));
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
