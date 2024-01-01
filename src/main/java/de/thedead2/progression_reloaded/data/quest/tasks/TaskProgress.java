package de.thedead2.progression_reloaded.data.quest.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TaskProgress implements Comparable<TaskProgress> {

    private final Map<String, CriterionProgress> criteria;

    private final CriteriaStrategy criteriaStrategy;


    public TaskProgress(QuestTask task) {
        this(Maps.newHashMap(), task.getCriteriaStrategy());
    }


    private TaskProgress(Map<String, CriterionProgress> criteria, CriteriaStrategy strategy) {
        this.criteria = criteria;
        this.criteriaStrategy = strategy;
    }


    public static TaskProgress loadFromNBT(CompoundTag tag) {
        Map<String, CriterionProgress> criteria = CollectionHelper.loadFromNBT(tag.getCompound("criteria"), s -> s, tag1 -> CriterionProgress.loadFromNBT((CompoundTag) tag1));
        CriteriaStrategy strategy = CriteriaStrategy.valueOf(tag.getString("strategy"));

        return new TaskProgress(criteria, strategy);
    }


    public static TaskProgress fromNetwork(FriendlyByteBuf buf) {
        Map<String, CriterionProgress> map = buf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        CriteriaStrategy strategy = buf.readEnum(CriteriaStrategy.class);
        return new TaskProgress(map, strategy);
    }


    public static TaskProgress fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Map<String, CriterionProgress> criteria = CollectionHelper.loadFromJson(jsonObject.getAsJsonObject("criteria"), s -> s, CriterionProgress::fromJson);
        CriteriaStrategy strategy = CriteriaStrategy.valueOf(jsonObject.get("strategy").getAsString());

        return new TaskProgress(criteria, strategy);
    }


    public boolean isDone() {
        return this.criteriaStrategy.isDone(this);
    }


    public boolean hasProgress() {
        for(CriterionProgress criterionprogress : this.criteria.values()) {
            if(criterionprogress.isDone()) {
                return true;
            }
        }

        return false;
    }


    public void updateProgress(QuestTask task) {
        Set<String> set = task.getCriteria().keySet();
        this.criteria.entrySet().removeIf((entry) -> !set.contains(entry.getKey()));

        for(String s : set) {
            if(!this.criteria.containsKey(s)) {
                this.criteria.put(s, new CriterionProgress());
            }
        }
    }


    public float getPercent() {
        if(this.criteria.isEmpty()) {
            return 0.0F;
        }
        else {
            int f = this.criteria.size();
            int f1 = this.countCompletedCriteria();
            return (float) f1 / f;
        }
    }


    private int countCompletedCriteria() {
        int i = 0;

        for(String s : criteria.keySet()) {
            CriterionProgress criterionprogress = this.getCriterion(s);
            if(criterionprogress != null && criterionprogress.isDone()) {
                i++;
            }
        }

        return i;
    }


    @Nullable
    public CriterionProgress getCriterion(String criterionName) {
        return this.criteria.get(criterionName);
    }


    public Iterable<String> getRemainingCriteria() {
        List<String> list = Lists.newArrayList();

        for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if(!entry.getValue().isDone()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }


    public Iterable<String> getCompletedCriteria() {
        List<String> list = Lists.newArrayList();

        for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if(entry.getValue().isDone()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }


    public int compareTo(TaskProgress taskProgress) {
        Date date = this.getFirstProgressDate();
        Date date1 = taskProgress.getFirstProgressDate();
        if(date == null && date1 != null) {
            return 1;
        }
        else if(date != null && date1 == null) {
            return -1;
        }
        else {
            return date == null ? 0 : date.compareTo(date1);
        }
    }


    @Nullable
    public Date getFirstProgressDate() {
        Date date = null;

        for(CriterionProgress criterionprogress : this.criteria.values()) {
            if(criterionprogress.isDone() && (date == null || criterionprogress.getObtained().before(date))) {
                date = criterionprogress.getObtained();
            }
        }

        return date;
    }


    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("criteria", CollectionHelper.saveToNBT(this.criteria, s -> s, CriterionProgress::saveToCompoundTag));
        tag.putString("strategy", this.criteriaStrategy.name());
        return tag;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf1, criterionProgress) -> criterionProgress.toNetwork(buf1));
        buf.writeEnum(this.criteriaStrategy);
    }


    public void reset() {
        this.criteria.forEach((s, criterionProgress) -> this.revokeProgress(s));
    }


    /**
     * Revokes the given criterion for this progress
     **/
    public boolean revokeProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if(criterionprogress != null && criterionprogress.isDone()) {
            criterionprogress.revoke();
            return true;
        }
        else {
            return false;
        }
    }


    public Map<String, CriterionProgress> getCriteria() {
        return criteria;
    }


    public void complete() {
        this.criteria.forEach((s, criterionProgress) -> this.grantProgress(s));
    }


    /**
     * Grants the given criterion to this progress
     **/
    public boolean grantProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if(criterionprogress != null && !criterionprogress.isDone()) {
            criterionprogress.grant();
            return true;
        }
        else {
            return false;
        }
    }


    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("criteria", CollectionHelper.saveToJson(this.criteria, s -> s, CriterionProgress::serializeToJson));
        jsonObject.addProperty("strategy", this.criteriaStrategy.name());

        return jsonObject;
    }
}
