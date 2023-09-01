package de.thedead2.progression_reloaded.data.criteria;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;

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

        Map<String, SimpleTrigger<?>> questCriteria = new HashMap<>();
        jsonObject.get("criteria").getAsJsonArray().forEach(jsonElement1 -> {
            JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
            questCriteria.put(jsonObject1.get("name").getAsString(), SimpleTrigger.fromJson(jsonObject1.get("trigger")));
        });

        return new QuestCriteria(questCriteria, criteriaStrategy1);
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("strategy", this.criteriaStrategy.toString());

        JsonArray jsonArray1 = new JsonArray();
        this.criteria.forEach((s, trigger) -> {
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("name", s);
            jsonObject1.add("trigger", trigger.toJson());
            jsonArray1.add(jsonObject1);
        });
        jsonObject.add("criteria", jsonArray1);

        return jsonObject;
    }


    public Map<String, SimpleTrigger<?>> getCriteria() {
        return this.criteria;
    }


    public CriteriaStrategy getCriteriaStrategy() {
        return this.criteriaStrategy;
    }


    public static class Builder {

        private final Map<String, SimpleTrigger<?>> criteria = new HashMap<>();

        private CriteriaStrategy criteriaStrategy = CriteriaStrategy.AND;


        private Builder() {}


        public static Builder builder() {
            return new Builder();
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
            return new QuestCriteria(this.criteria, this.criteriaStrategy);
        }
    }
}
