package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Progress of a quest is dependent on the player or the team. Different players or teams can have different progress of a quest.
 * **/

public class QuestProgress implements Comparable<QuestProgress>{
    private final Map<String, CriterionProgress> criteria;
    private final CriteriaStrategy criteriaStrategy;

    /** Corresponding quest of this Progress**/
    private final ProgressionQuest quest;

    private QuestProgress(Map<String, CriterionProgress> criteria, ProgressionQuest quest) {
        this.criteria = criteria;
        this.quest = quest;
        this.criteriaStrategy = this.quest.getCriteriaStrategy();
    }

    public QuestProgress(ProgressionQuest quest) {
        this(Maps.newHashMap(), quest);
    }

    public static QuestProgress loadFromCompoundTag(CompoundTag tag) {
        Map<String, CriterionProgress> criteria = new HashMap<>();
        ProgressionQuest quest;

        tag.getAllKeys().stream().filter(s -> !s.equals("quest")).forEach(s -> criteria.put(s, CriterionProgress.loadFromCompoundTag(tag.getCompound(s))));
        quest = ModRegistries.QUESTS.get().getValue(new ResourceLocation(tag.getString("quest")));

        return new QuestProgress(criteria, quest);
    }


    public void updateProgress() {
        Set<String> set = this.quest.getCriteria().keySet();
        this.criteria.entrySet().removeIf((entry) -> !set.contains(entry.getKey()));

        for(String s : set) {
            if (!this.criteria.containsKey(s)) {
                this.criteria.put(s, new CriterionProgress());
            }
        }
    }

    public boolean isDone() {
        boolean flag = false;
        for(String s : criteria.keySet()){
            CriterionProgress criterionprogress = this.getCriterion(s);
            if (criterionprogress != null && criterionprogress.isDone()) {
                flag = true;
                if(this.criteriaStrategy.equals(CriteriaStrategy.OR)) break;
            }
            else {
                flag = false;
                break;
            }
        }

        return flag;
    }

    public boolean hasProgress() {
        for(CriterionProgress criterionprogress : this.criteria.values()) {
            if (criterionprogress.isDone()) {
                return true;
            }
        }

        return false;
    }

    /** Grants the given criterion to this progress **/
    public boolean grantProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && !criterionprogress.isDone()) {
            criterionprogress.grant();
            return true;
        } else {
            return false;
        }
    }

    /** Revokes the given criterion for this progress **/
    public boolean revokeProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && criterionprogress.isDone()) {
            criterionprogress.revoke();
            return true;
        } else {
            return false;
        }
    }


    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf1, criterionProgress) -> {
            criterionProgress.serializeToNetwork(buf1);
        });
    }

    @Nullable
    public CriterionProgress getCriterion(String criterionName) {
        return this.criteria.get(criterionName);
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0F;
        } else {
            float f = (float) this.criteria.size();
            float f1 = (float) this.countCompletedCriteria();
            return f1 / f;
        }
    }

    private int countCompletedCriteria() {
        int i = 0;

        for(String s : criteria.keySet()){
            CriterionProgress criterionprogress = this.getCriterion(s);
            if (criterionprogress != null && criterionprogress.isDone()) {
                i++;
            }
        }

        return i;
    }

    public Iterable<String> getRemainingCriteria() {
        List<String> list = Lists.newArrayList();

        for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (!entry.getValue().isDone()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }

    public Iterable<String> getCompletedCriteria() {
        List<String> list = Lists.newArrayList();

        for(Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (entry.getValue().isDone()) {
                list.add(entry.getKey());
            }
        }

        return list;
    }

    @Nullable
    public Date getFirstProgressDate() {
        Date date = null;

        for(CriterionProgress criterionprogress : this.criteria.values()) {
            if (criterionprogress.isDone() && (date == null || criterionprogress.getObtained().before(date))) {
                date = criterionprogress.getObtained();
            }
        }

        return date;
    }

    public int compareTo(QuestProgress questProgress) {
        Date date = this.getFirstProgressDate();
        Date date1 = questProgress.getFirstProgressDate();
        if (date == null && date1 != null) {
            return 1;
        } else if (date != null && date1 == null) {
            return -1;
        } else {
            return date == null ? 0 : date.compareTo(date1);
        }
    }

    public void complete() {
        this.criteria.forEach((s, criterionProgress) -> this.grantProgress(s));
    }

    public void reset(){
        this.criteria.forEach((s, criterionProgress) -> this.revokeProgress(s));
    }

    public ProgressionQuest getQuest() {
        return this.quest;
    }

    public CompoundTag saveToCompoundTag() {
        CompoundTag tag = new CompoundTag();
        this.criteria.forEach((s, criterionProgress) -> {
            tag.put(s, criterionProgress.saveToCompoundTag());
        });
        tag.putString("quest", this.quest.getId().toString());
        return tag;
    }
}
