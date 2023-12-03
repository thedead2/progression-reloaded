package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import net.minecraft.resources.ResourceLocation;


public class QuestPredicate implements ITriggerPredicate<ProgressionQuest> {

    private final ResourceLocation questId;


    public QuestPredicate(ResourceLocation questId) {
        this.questId = questId;
    }


    public static QuestPredicate fromJson(JsonElement jsonElement) {
        return new QuestPredicate(new ResourceLocation(jsonElement.getAsString()));
    }


    @Override
    public boolean matches(ProgressionQuest quest, Object... addArgs) {
        return quest.getId().equals(this.questId);
    }


    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.questId.toString());
    }
}
