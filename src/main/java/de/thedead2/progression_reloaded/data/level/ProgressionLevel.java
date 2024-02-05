package de.thedead2.progression_reloaded.data.level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.api.IProgressable;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.display.LevelDisplayInfo;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.rewards.Rewards;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;


public class ProgressionLevel implements IProgressable<ProgressionLevel> {

    private final LevelDisplayInfo displayInfo;

    private final Rewards rewards;

    private final Set<ResourceLocation> quests;


    public ProgressionLevel(LevelDisplayInfo displayInfo, Rewards rewards, Set<ResourceLocation> quests) {
        this.displayInfo = displayInfo;
        this.rewards = rewards;
        this.quests = quests;
    }


    public static ProgressionLevel fromKey(ResourceLocation level) {
        return ModRegistries.LEVELS.get().getValue(level);
    }


    public static ProgressionLevel fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        LevelDisplayInfo displayInfo = LevelDisplayInfo.fromJson(jsonObject.get("display"));
        Rewards rewards = Rewards.fromJson(jsonObject.get("rewards"));
        Set<ResourceLocation> levelQuests = CollectionHelper.loadFromJson(Sets::newHashSetWithExpectedSize, jsonObject.getAsJsonArray("quests"), jsonElement1 -> new ResourceLocation(jsonElement1.getAsString()));

        return new ProgressionLevel(displayInfo, rewards, levelQuests);
    }


    public static ProgressionLevel fromNetwork(FriendlyByteBuf buf) {
        LevelDisplayInfo displayInfo = LevelDisplayInfo.fromNetwork(buf);
        Rewards rewards = Rewards.fromNetwork(buf);
        Set<ResourceLocation> quests = buf.readCollection(Sets::newHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);

        return new ProgressionLevel(displayInfo, rewards, quests);
    }


    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("display", this.displayInfo.toJson());
        jsonObject.add("rewards", this.rewards.toJson());
        jsonObject.add("quests", CollectionHelper.saveToJson(this.quests, id -> new JsonPrimitive(id.toString())));

        return jsonObject;
    }


    @Override
    public ResourceLocation getId() {
        return this.displayInfo.id();
    }


    public boolean contains(ProgressionLevel other) {
        if(this.equals(LevelManager.CREATIVE)) {
            return true;
        }

        ProgressionLevel previousLevel = this.getParent() != null ? ModRegistries.LEVELS.get().getValue(this.getParent()) : null;
        if(this.equals(other) || (previousLevel != null && previousLevel.equals(other))) {
            return true;
        }
        else if(previousLevel == null) {
            return false;
        }
        else {
            return previousLevel.contains(other);
        }
    }


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        this.displayInfo.toNetwork(buf);
        this.rewards.toNetwork(buf);
        buf.writeCollection(this.quests, FriendlyByteBuf::writeResourceLocation);
    }


    @Nullable
    public ResourceLocation getParent() {
        return this.displayInfo.previousLevel();
    }


    @Override
    public int compareTo(@NotNull ProgressionLevel o) {
        ResourceLocation thisId = this.getId();
        ResourceLocation otherId = o.getId();

        ResourceLocation thisPrevious = this.getParent();
        ResourceLocation otherPrevious = o.getParent();

        if(thisId.equals(otherId)) { // same levels
            return 0;
        }
        else if(thisPrevious != null && thisPrevious.equals(otherId)) {
            return 1; // this is greater
        }
        else if(otherPrevious != null && otherPrevious.equals(thisId)) {
            return -1; // this is less
        }
        else {
            throw new IllegalArgumentException("Can't compare level " + thisId + " with level " + otherId + " as they are not related to each other!");
        }
    }


    public boolean hasParent() {
        return this.displayInfo.previousLevel() != null;
    }


    public Component getTitle() {
        return this.displayInfo.title();
    }


    public void rewardPlayer(PlayerData player) {
        this.rewards.reward(player);
    }


    public ImmutableSet<ResourceLocation> getQuests() {
        return ImmutableSet.copyOf(this.quests);
    }


    public LevelDisplayInfo getDisplay() {
        return this.displayInfo;
    }


    public Rewards getRewards() {
        return this.rewards;
    }


    public boolean contains(ProgressionQuest quest) {
        ProgressionLevel previousLevel = this.getParent() != null ? ModRegistries.LEVELS.get().getValue(this.getParent()) : null;
        return this.quests.contains(quest.getId()) || (previousLevel != null && previousLevel.contains(quest));
    }
}
