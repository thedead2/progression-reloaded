package de.thedead2.progression_reloaded.data.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.data.predicates.QuestPredicate;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.events.QuestEvent;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class QuestCompleteCriterionTrigger extends SimpleCriterionTrigger<ProgressionQuest> {

    public static final ResourceLocation ID = createId("quest_complete");


    public QuestCompleteCriterionTrigger(QuestPredicate questPredicate) {
        this(questPredicate, MinMax.Doubles.ANY);
    }


    public QuestCompleteCriterionTrigger(QuestPredicate questPredicate, MinMax.Doubles duration) {
        super(ID, questPredicate, MinMax.Ints.ANY, duration, "completed_quest");
    }


    protected static QuestCompleteCriterionTrigger fromJson(JsonElement jsonElement, MinMax.Ints amount, MinMax.Doubles duration) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        QuestPredicate quest = QuestPredicate.fromJson(jsonObject.get("completed_quest"));
        return new QuestCompleteCriterionTrigger(quest, duration);
    }


    @SubscribeEvent
    public static void onQuestComplete(QuestEvent.CompletionEvent event) {
        fireTrigger(QuestCompleteCriterionTrigger.class, event.getPlayer().getPlayer(), event.getQuest(), event.getCompletionStatus());
    }


    @Override
    public boolean trigger(PlayerData player, ProgressionQuest quest, Object... data) {
        return this.trigger(player, trigger -> this.predicate.matches(quest, data[0]));
    }

    @Override
    public Component getDefaultDescription() {
        return Component.literal("Complete ").append(this.predicate.getDefaultDescription());
    }
}
