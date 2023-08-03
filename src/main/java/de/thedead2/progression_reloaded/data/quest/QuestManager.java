package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface QuestManager {
    JsonElement toJson();

    boolean award(ProgressionQuest quest, String criterionName, SinglePlayer player);

    boolean award(ResourceLocation quest, String criterionName, SinglePlayer player);
    boolean award(ResourceLocation quest, SinglePlayer player);
    boolean award(ProgressionQuest quest, SinglePlayer player);

    boolean revoke(ProgressionQuest quest, SinglePlayer player, String criterionName);

    <T extends SimpleTrigger> void fireTriggers(Class<T> triggerClass, SinglePlayer player, Object... data);

    QuestProgress getProgress(ProgressionQuest quest, SinglePlayer player);

    ImmutableSet<QuestProgress> getMainQuestProgressFor(SinglePlayer player);

    ImmutableMap<ResourceLocation, ProgressionQuest> getQuests();

    QuestManager convert();

    boolean isEmpty();

    int size();
    void reloadData();

    void saveData();

    void updateStatus(SinglePlayer player);
    void stopListening(SinglePlayer player);

    //void loadAdditionalActiveQuests(QuestManager oldManager, SinglePlayer player);

    Map<ProgressionQuest, QuestProgress> getRemainingQuests(SinglePlayer player);

    ResourceLocation getLevel();
}
