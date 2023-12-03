package de.thedead2.progression_reloaded.data.criteria;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.predicates.PlayerPredicate;
import de.thedead2.progression_reloaded.data.predicates.QuestPredicate;
import de.thedead2.progression_reloaded.data.predicates.TimePredicate;
import de.thedead2.progression_reloaded.data.trigger.QuestCompleteTrigger;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;
import de.thedead2.progression_reloaded.data.trigger.TickTrigger;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class QuestCriteria {

    private final Map<String, SimpleTrigger<?>> criteria;

    private final CriteriaStrategy criteriaStrategy;


    public QuestCriteria(Map<String, SimpleTrigger<?>> criteria, CriteriaStrategy criteriaStrategy) {
        this.criteria = criteria;
        this.criteriaStrategy = criteriaStrategy;
    }


    public static QuestCriteria fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        CriteriaStrategy criteriaStrategy1 = CriteriaStrategy.valueOf(jsonObject.get("strategy").getAsString());
        Map<String, SimpleTrigger<?>> questCriteria = CollectionHelper.loadFromJson(jsonObject.getAsJsonObject("criteria"), s -> s, SimpleTrigger::fromJson);

        return new QuestCriteria(questCriteria, criteriaStrategy1);
    }


    public static QuestCriteria loadFromNBT(CompoundTag tag) {
        CriteriaStrategy strategy = CriteriaStrategy.valueOf(tag.getString("strategy"));
        Map<String, SimpleTrigger<?>> criteria = CollectionHelper.loadFromNBT(tag.getCompound("criteria"), s -> s, tag1 -> SimpleTrigger.loadFromNBT((CompoundTag) tag1));

        return new QuestCriteria(criteria, strategy);
    }


    public static QuestCriteria fromNetwork(FriendlyByteBuf buf) {
        CriteriaStrategy strategy = buf.readEnum(CriteriaStrategy.class);
        Map<String, SimpleTrigger<?>> criteria = buf.readMap(FriendlyByteBuf::readUtf, SimpleTrigger::fromNetwork);

        return new QuestCriteria(criteria, strategy);
    }


    public static QuestCriteria empty() {
        return Builder.builder().build();
    }


    public static QuestCriteria requiresParentComplete(String parentId) {
        return Builder.builder().withParentComplete(parentId).build();
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("strategy", this.criteriaStrategy.name());
        jsonObject.add("criteria", CollectionHelper.saveToJson(this.criteria, s -> s, SimpleTrigger::toJson));

        return jsonObject;
    }


    public Map<String, SimpleTrigger<?>> getCriteria() {
        return this.criteria;
    }


    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteriaStrategy;
    }


    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString("strategy", this.criteriaStrategy.name());
        tag.put("criteria", CollectionHelper.saveToNBT(this.criteria, s -> s, SimpleTrigger::saveToNBT));

        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeEnum(this.criteriaStrategy);
        buf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf1, trigger) -> trigger.toNetwork(buf1));
    }


    public static class Builder {

        private final Map<String, SimpleTrigger<?>> criteria = new HashMap<>();

        private CriteriaStrategy criteriaStrategy = CriteriaStrategy.AND;


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withParentComplete(String questId) {
            return this.withCriterion("parent_complete", new QuestCompleteTrigger(PlayerPredicate.ANY, new QuestPredicate(new ResourceLocation(ModHelper.MOD_ID, questId))));
        }


        public Builder withCriterion(String name, SimpleTrigger<?> criterion) {
            this.criteria.put(name, criterion);

            return this;
        }


        public Builder withStrategy(CriteriaStrategy strategy) {
            this.criteriaStrategy = strategy;

            return this;
        }


        public QuestCriteria build() {
            if(this.criteria.isEmpty()) {
                this.criteria.put("nothing", new TickTrigger(PlayerPredicate.ANY, new TimePredicate(0)));
            }
            return new QuestCriteria(this.criteria, this.criteriaStrategy);
        }
    }
}
