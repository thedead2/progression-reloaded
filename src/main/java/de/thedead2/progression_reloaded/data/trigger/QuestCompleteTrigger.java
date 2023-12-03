package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.data.predicates.QuestPredicate;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.events.QuestEvent;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class QuestCompleteTrigger extends SimpleTrigger<ProgressionQuest> {

    public static final ResourceLocation ID = createId("quest_complete");


    public QuestCompleteTrigger(PlayerPredicate player, QuestPredicate questPredicate) {
        super(ID, player, questPredicate, "completed_quest");
    }


    public static QuestCompleteTrigger fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        PlayerPredicate player = PlayerPredicate.fromJson(jsonObject.get("player"));
        QuestPredicate quest = QuestPredicate.fromJson(jsonObject.get("completed_quest"));
        return new QuestCompleteTrigger(player, quest);
    }


    @SubscribeEvent
    public static void onQuestComplete(QuestEvent.AwardQuestEvent event) {
        fireTrigger(QuestCompleteTrigger.class, event.getPlayer().getPlayer(), event.getQuest());
    }


    @Override
    public boolean trigger(PlayerData player, ProgressionQuest quest, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(quest, data));
    }


    @Override
    public void toJson(JsonObject data) {}
}
