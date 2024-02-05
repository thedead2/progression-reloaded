package de.thedead2.progression_reloaded.data.trigger;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.predicates.MinMax;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.tasks.TaskProgress;
import de.thedead2.progression_reloaded.data.tasks.types.QuestTask;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import de.thedead2.progression_reloaded.util.registries.TypeRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public abstract class SimpleCriterionTrigger<T> {

    protected final MinMax.Ints amount;

    protected final ITriggerPredicate<T> predicate;

    protected final MinMax.Doubles duration;

    private final ResourceLocation id;

    private final String predicateName;

    private final Multimap<PlayerData, Listener> playerListeners = HashMultimap.create();


    protected SimpleCriterionTrigger(ResourceLocation id, ITriggerPredicate<T> predicate, MinMax.Ints amount, MinMax.Doubles duration, String predicateName) {
        this.id = id;
        this.predicate = predicate;
        this.amount = amount;
        this.duration = duration;
        this.predicateName = predicateName;
    }


    protected static <T> void fireTrigger(Class<? extends SimpleCriterionTrigger<T>> triggerClass, Entity entity, T toTest, Object... addArgs) {
        if(entity instanceof Player player) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            PlayerQuests playerQuests = playerData.getPlayerQuests();

            playerQuests.getQuestsByStatus(QuestStatus.NOT_STARTED).forEach(quest -> {
                QuestProgress questProgress = playerQuests.getOrStartProgress(quest);
                Set<QuestTask> tasks = questProgress.getPotentialStartingTasks();

                tasks.forEach(task -> checkTriggers(task, triggerClass, playerData, toTest, addArgs));
            });

            playerQuests.getStartedOrActiveQuests().forEach(quest -> {
                QuestProgress questProgress = playerQuests.getOrStartProgress(quest);

                questProgress.getChildrenForCurrentTask().forEach(task -> checkTriggers(task, triggerClass, playerData, toTest, addArgs));
            });
        }
    }


    private static <T> void checkTriggers(QuestTask task, Class<? extends SimpleCriterionTrigger<T>> triggerClass, PlayerData player, T toTest, Object... data) {
        if(task.getCriterion().getClass().equals(triggerClass)) {
            SimpleCriterionTrigger<T> trigger = triggerClass.cast(task.getCriterion());

            boolean triggerTestResult = trigger.trigger(player, toTest, data);
            if(!PREventFactory.onTriggerFiring(trigger, player, toTest, data, triggerTestResult) && triggerTestResult) {
                LevelManager.getInstance().updateStatus(player);
            }
        }
    }


    protected static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_trigger");
    }


    public static <T extends SimpleCriterionTrigger<T>> T fromNetwork(FriendlyByteBuf buf) {
        return loadFromNBT(buf.readNbt());
    }


    public static <T extends SimpleCriterionTrigger<T>> T loadFromNBT(CompoundTag tag) {
        return fromJson(SerializationHelper.convertToJson(tag));
    }


    @SuppressWarnings("unchecked")
    public static <T extends SimpleCriterionTrigger<T>> T fromJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ResourceLocation resourceLocation = new ResourceLocation(jsonObject.get("id").getAsString());
            MinMax.Ints count = MinMax.Ints.fromJson(jsonObject.get("amount"));
            MinMax.Doubles duration = MinMax.Doubles.fromJson(jsonObject.get("duration"));
            Class<SimpleCriterionTrigger<?>> triggerClass = TypeRegistries.PROGRESSION_TRIGGER.get(resourceLocation);
            return (T) SerializationHelper.createGeneric(triggerClass, jsonObject, count, duration);
        }
        else {
            throw new IllegalArgumentException("Can't read data from: " + jsonElement);
        }
    }


    public MinMax.Ints getAmount() {
        return amount;
    }


    public MinMax.Doubles getDuration() {
        return duration;
    }


    public final void addListener(PlayerData player, Listener listener) {
        this.playerListeners.put(player, listener);
    }


    public final void removeListener(PlayerData player, Listener listener) {
        this.playerListeners.remove(player, listener);
    }


    protected boolean trigger(PlayerData player, Predicate<Listener> triggerPredicate) {
        List<Listener> list = Lists.newArrayList();
        boolean flag = false;

        for(Listener listener : this.playerListeners.get(player)) {
            if(triggerPredicate.test(listener)) {
                list.add(listener);
                flag = true;
            }
        }

        for(Listener listener1 : list) {
            listener1.onComplete(player);
        }

        return flag;
    }


    public abstract boolean trigger(PlayerData player, T toTest, Object... data);


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeNbt((CompoundTag) SerializationHelper.convertToNBT(this.toJson()));
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("id", new JsonPrimitive(this.id.toString()));
        SerializationHelper.addNullable(this.predicate, jsonObject, this.predicateName, ITriggerPredicate::toJson);
        jsonObject.add("amount", this.amount.toJson());
        jsonObject.add("duration", this.duration.toJson());
        this.toJson(jsonObject);

        return jsonObject;
    }


    protected void toJson(JsonObject jsonObject) {}


    public abstract Component getDefaultDescription();

    public static class Listener {

        private final ProgressionQuest quest;

        private final QuestTask task;


        public Listener(ProgressionQuest quest, QuestTask task) {
            this.quest = quest;
            this.task = task;
        }


        public void onComplete(PlayerData player) {
            player.getPlayerQuests().getOrStartProgress(this.quest).updateProgress(this.task, TaskProgress.UpdateMode.UPDATE);
        }


        public QuestTask getTask() {
            return task;
        }


        @Override
        public int hashCode() {
            return Objects.hashCode(task);
        }


        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            Listener listener = (Listener) o;
            return Objects.equal(task, listener.task);
        }
    }
}
