package de.thedead2.progression_reloaded.data.trigger;

import com.google.common.base.Objects;
import com.google.common.collect.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.predicates.ITriggerPredicate;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.registries.DynamicRegistries;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SimpleTrigger {
    protected final ResourceLocation id;
    protected final PlayerPredicate player;
    private final Multimap<KnownPlayer, Listener> playerListeners = HashMultimap.create();
    public void addListener(KnownPlayer player, Listener listener){
        this.playerListeners.put(player, listener);
    }

    public void removeListener(KnownPlayer player, Listener listener){
        this.playerListeners.get(player).remove(listener);
    }

    public static void register(ResourceLocation id, Class<SimpleTrigger> trigger){
        DynamicRegistries.register(id, trigger, DynamicRegistries.PROGRESSION_TRIGGER, SimpleTrigger.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SimpleTrigger> T fromJson(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()){
            ResourceLocation resourceLocation = new ResourceLocation(((JsonObject) jsonElement).get("id").getAsString());
            Class<? extends SimpleTrigger> triggerClass = DynamicRegistries.PROGRESSION_TRIGGER.get(resourceLocation);
            try {
                return (T) triggerClass.getDeclaredMethod("fromJson", JsonElement.class).invoke(null, ((JsonObject) jsonElement).get("data").getAsJsonObject());
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
                throw new RuntimeException(e);
            }
        }
        else throw new IllegalArgumentException("Can't read data from: " + jsonElement);
    }

    protected SimpleTrigger(ResourceLocation id, PlayerPredicate player){
        this.id = id;
        this.player = player;
    }

    protected void trigger(SinglePlayer player, Predicate<SimpleTrigger> triggerPredicate) {
        List<Listener> list = Lists.newArrayList();

        for(Listener listener : this.playerListeners.get(KnownPlayer.fromSinglePlayer(player))) {
            if (triggerPredicate.test(this) && this.player.matches(player)) {
                list.add(listener);
            }
        }

        for (Listener listener1 : list) {
            ModHelper.LOGGER.debug("Firing Trigger: " + this.getClass().getName());
            listener1.award(player);
        }
    }

    public abstract void trigger(SinglePlayer player, Object... data);

    protected static void fireTrigger(Class<? extends SimpleTrigger> triggerClass, Entity entity, Object... addArgs){
        if(entity instanceof Player player){
            SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(player);
            LevelManager.getInstance().getQuestManager().fireTriggers(triggerClass, singlePlayer, addArgs);
        }
    }

    public Iterable<ITriggerPredicate<?>> getPredicates(){
        Stream<ITriggerPredicate<?>> stream = Stream.of(this.player);
        return Stream.concat(stream,
                Arrays.stream(this.getClass().getDeclaredFields())
                .filter(field -> Arrays.stream(field.getType().getInterfaces()).anyMatch(aClass -> aClass.getName().equals(ITriggerPredicate.class.getName())))
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return (ITriggerPredicate<?>) field.get(this);
                    }
                    catch (IllegalAccessException e) {
                        CrashHandler.getInstance().handleException("Unable to get field value of field: " + field.getName(), e, Level.ERROR);
                        return null;
                    }
                })).collect(Collectors.toList());
    }
    public JsonElement toJson(){
        JsonObject jsonObject = new JsonObject();
            jsonObject.add("id", new JsonPrimitive(this.id.toString()));
            JsonObject data = new JsonObject();
                data.add("player", this.player.toJson());
                this.toJson(data);
            jsonObject.add("data", data);
        return jsonObject;
    }
    public abstract void toJson(JsonObject data);

    public static ResourceLocation createId(String name){
        return new ResourceLocation(ModHelper.MOD_ID, name + "_trigger");
    }

    public static class Listener {
        private final ProgressionQuest quest;
        private final String criterion;

        public Listener(ProgressionQuest quest, String criterionName) {
            this.quest = quest;
            this.criterion = criterionName;
        }

        public void award(SinglePlayer player) {
            LevelManager.getInstance().getQuestManager().award(this.quest, this.criterion, KnownPlayer.fromSinglePlayer(player));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Listener listener = (Listener) o;
            return Objects.equal(quest, listener.quest) && Objects.equal(criterion, listener.criterion);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(quest, criterion);
        }
    }
}
