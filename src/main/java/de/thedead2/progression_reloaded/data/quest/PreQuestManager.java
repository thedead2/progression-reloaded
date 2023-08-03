package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dummy QuestManager for Level Registration. Throws an exception if any other method than fromJson, toJson, covert and getLevel is called!
 * **/
public class PreQuestManager implements QuestManager {
    private final Set<ResourceLocation> quests;
    private final ResourceLocation levelId;
    private final RuntimeException EXCEPTION;

    public PreQuestManager(Set<ResourceLocation> quests, ResourceLocation levelId){
        this.quests = quests;
        this.levelId = levelId;
        EXCEPTION = new RuntimeException("Tried to access method of PreQuestManager for level " + this.levelId + "!\n This is unsupported behaviour!");
    }


    public static PreQuestManager fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        JsonArray array = jsonObject.get("quests").getAsJsonArray();
        Set<ResourceLocation> questIds = new HashSet<>();

        array.forEach(jsonElement1 -> questIds.add(new ResourceLocation(jsonElement1.getAsString())));
        ResourceLocation levelId = new ResourceLocation(jsonObject.get("level").getAsString());
        return new PreQuestManager(questIds, levelId);
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        this.quests.forEach(resourceLocation -> jsonArray.add(resourceLocation.toString()));
        jsonObject.addProperty("level", this.levelId.toString());
        jsonObject.add("quests", jsonArray);
        return jsonObject;
    }

    @Override
    public boolean award(ProgressionQuest quest, String criterionName, SinglePlayer player) {
        throw EXCEPTION;
    }

    @Override
    public boolean award(ResourceLocation quest, String criterionName, SinglePlayer player) {
        throw EXCEPTION;
    }

    @Override
    public boolean award(ResourceLocation quest, SinglePlayer player) {
        return award(quest, null, player);
    }

    @Override
    public boolean award(ProgressionQuest quest, SinglePlayer player) {
        return award(quest, null, player);
    }

    @Override
    public boolean revoke(ProgressionQuest quest, SinglePlayer player, String criterionName) {
        throw EXCEPTION;
    }

    @Override
    public <T extends SimpleTrigger> void fireTriggers(Class<T> triggerClass, SinglePlayer player, Object... data) {
        throw EXCEPTION;
    }

    @Override
    public QuestProgress getProgress(ProgressionQuest quest, SinglePlayer player) {
        throw EXCEPTION;
    }

    @Override
    public ImmutableSet<QuestProgress> getMainQuestProgressFor(SinglePlayer player) {
        throw EXCEPTION;
        //return ImmutableSet.copyOf(Collections.emptySet());
    }

    @Override
    public ImmutableMap<ResourceLocation, ProgressionQuest> getQuests() {
        throw EXCEPTION;
        //return ImmutableMap.copyOf(Collections.emptyMap());
    }

    @Override
    public ActiveQuestManager convert() {
        return new ActiveQuestManager(this.quests, this.levelId);
    }

    @Override
    public boolean isEmpty() {
        throw EXCEPTION;
    }

    @Override
    public int size() {
        throw EXCEPTION;
    }

    @Override
    public void reloadData() {
        throw EXCEPTION;
    }

    @Override
    public void saveData() {
        throw EXCEPTION;
    }

    @Override
    public void updateStatus(SinglePlayer player) {
        throw EXCEPTION;
    }

    @Override
    public void stopListening(SinglePlayer player) {
        throw EXCEPTION;
    }

    /*@Override
    public void loadAdditionalActiveQuests(QuestManager oldManager, SinglePlayer player) {
        throw EXCEPTION;
    }*/

    @Override
    public Map<ProgressionQuest, QuestProgress> getRemainingQuests(SinglePlayer player) {
        throw EXCEPTION;
    }

    @Override
    public ResourceLocation getLevel() {
        return this.levelId;
    }
}
