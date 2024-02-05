package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;


public class QuestPredicate implements ITriggerPredicate<ProgressionQuest> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("quest");

    private final ResourceLocation questId;

    @Nullable
    private final QuestStatus completionStatus;


    public QuestPredicate(ResourceLocation questId, @Nullable QuestStatus completionStatus) {
        this.questId = questId;
        this.completionStatus = completionStatus;
    }


    public static QuestPredicate fromJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            return new QuestPredicate(new ResourceLocation(jsonObject.get("quest").getAsString()), QuestStatus.valueOf(jsonObject.get("completionStatus").getAsString()));
        }
        else {
            return new QuestPredicate(new ResourceLocation(jsonElement.getAsString()), null);
        }
    }


    public static QuestPredicate from(ResourceLocation questId) {
        return new QuestPredicate(questId, null);
    }


    @Override
    public boolean matches(ProgressionQuest quest, Object... addArgs) {
        if(this.questId.equals(quest.getId())) {
            if(this.completionStatus != null) {
                return this.completionStatus.equals(addArgs[0]);
            }
            else {
                return true;
            }
        }
        return false;
    }


    @Override
    public JsonElement toJson() {
        if(this.completionStatus == null) {
            return new JsonPrimitive(this.questId.toString());
        }
        else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("quest", this.questId.toString());
            jsonObject.addProperty("completionStatus", this.completionStatus.name());

            return jsonObject;
        }
    }


    @Override
    public Component getDefaultDescription() {
        return ((MutableComponent) ModRegistries.QUESTS.get().getValue(this.questId).getTitle()).append(this.completionStatus == QuestStatus.COMPLETE ? " successfully" : " unsuccessfully");
    }
}
