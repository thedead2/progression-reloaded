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
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.data.quest.QuestProgress;
import de.thedead2.progression_reloaded.data.quest.QuestStatus;
import de.thedead2.progression_reloaded.data.quest.tasks.QuestTask;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.SerializationHelper;
import de.thedead2.progression_reloaded.util.registries.TypeRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public abstract class SimpleTrigger<T> {

    protected final ResourceLocation id;

    protected final PlayerPredicate player;

    protected final ITriggerPredicate<T> predicate;

    protected final String predicateName;

    private final Multimap<PlayerData, Listener> playerListeners = HashMultimap.create();


    protected SimpleTrigger(ResourceLocation id, PlayerPredicate player, ITriggerPredicate<T> predicate, String predicateName) {
        this.id = id;
        this.player = player;
        this.predicate = predicate;
        this.predicateName = predicateName;
    }


    @SuppressWarnings("unchecked")
    public static <T extends SimpleTrigger<T>> T fromJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            ResourceLocation resourceLocation = new ResourceLocation(((JsonObject) jsonElement).get("id").getAsString());
            Class<? extends SimpleTrigger<?>> triggerClass = TypeRegistries.PROGRESSION_TRIGGER.get(resourceLocation);
            try {
                return (T) triggerClass.getDeclaredMethod("fromJson", JsonElement.class).invoke(null, ((JsonObject) jsonElement).get("data").getAsJsonObject());
            }
            catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new IllegalArgumentException("Can't read data from: " + jsonElement);
        }
    }


    protected static <T> void fireTrigger(Class<? extends SimpleTrigger<T>> triggerClass, Entity entity, T toTest, Object... addArgs) {
        if(entity instanceof Player player) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);

            PlayerQuests playerQuests = playerData.getQuestData();

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


    @SuppressWarnings("unchecked")
    private static <T> void checkTriggers(QuestTask task, Class<? extends SimpleTrigger<T>> triggerClass, PlayerData player, T toTest, Object... data) {
        task.getCriteria()
            .values()
            .stream()
            .filter(simpleTrigger -> simpleTrigger.getClass().equals(triggerClass))
            .forEach(trigger -> {
                boolean triggerTestResult = ((SimpleTrigger<T>) trigger).trigger(player, toTest, data);
                if(!PREventFactory.onTriggerFiring((SimpleTrigger<T>) trigger, player, toTest, data, triggerTestResult) && triggerTestResult) {
                    LevelManager.getInstance().updateStatus(player);
                }
            });
    }


    public static ResourceLocation createId(String name) {
        return new ResourceLocation(ModHelper.MOD_ID, name + "_trigger");
    }


    public static <T extends SimpleTrigger<T>> T fromNetwork(FriendlyByteBuf buf) {
        return loadFromNBT(buf.readNbt());
    }


    public static <T extends SimpleTrigger<T>> T loadFromNBT(CompoundTag tag) {
        return fromJson(SerializationHelper.convertToJson(tag));
    }


    public final void addListener(PlayerData player, Listener listener) {
        this.playerListeners.put(player, listener);
    }


    public final void removeListener(PlayerData player, Listener listener) {
        this.playerListeners.remove(player, listener);
    }


    protected boolean trigger(PlayerData player, Predicate<Listener> triggerPredicate) {
        List<Listener> list = Lists.newArrayList();

        for(Listener listener : this.playerListeners.get(player)) {
            if(triggerPredicate.test(listener) && this.player.matches(player)) {
                list.add(listener);
            }
        }

        boolean flag = false;
        for(Listener listener1 : list) {
            flag = listener1.onComplete(player);
        }
        return flag;
    }


    public abstract boolean trigger(PlayerData player, T toTest, Object... data);


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("id", new JsonPrimitive(this.id.toString()));
        JsonObject data = new JsonObject();
        //data.add("player", this.player.toJson());
        if(this.predicate != null) {
            data.add(this.predicateName, this.predicate.toJson());
        }
        this.toJson(data);
        jsonObject.add("data", data);
        return jsonObject;
    }


    public abstract void toJson(JsonObject data);


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeNbt(this.saveToNBT());
    }


    public CompoundTag saveToNBT() {
        return (CompoundTag) SerializationHelper.convertToNBT(this.toJson());
    }

    /*@OnlyIn(Dist.CLIENT)
    public abstract TriggerComponent<? extends SimpleTrigger<T>> getDisplayComponent(Area area);*/

    /**
     *
     **/
    /*@OnlyIn(Dist.CLIENT)
    public abstract Component getShortDescription();*/

    public static class Listener {

        private final ProgressionQuest quest;

        private final QuestTask task;

        private final String criterion;


        public Listener(ProgressionQuest quest, QuestTask task, String criterionName) {
            this.quest = quest;
            this.task = task;
            this.criterion = criterionName;
        }


        public boolean onComplete(PlayerData player) {
            return player.getQuestData().getOrStartProgress(this.quest).award(this.task, this.criterion, player);
        }


        public QuestTask getTask() {
            return task;
        }


        @Override
        public int hashCode() {
            return Objects.hashCode(task, criterion);
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
            return Objects.equal(task, listener.task) && Objects.equal(criterion, listener.criterion);
        }
    }
}
