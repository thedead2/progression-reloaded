package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.api.progress.IProgressInfo;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.*;


/**
 * Progress of a quest is dependent on the player or the team. Different players or teams can have different progress of a quest.
 **/

public class QuestProgress implements Comparable<QuestProgress>, IProgressInfo {

    private final Map<String, CriterionProgress> criteria;

    private final CriteriaStrategy criteriaStrategy;


    public QuestProgress(ProgressionQuest quest) {
        this(Maps.newHashMap(), quest.getCriteriaStrategy());
    }


    private QuestProgress(Map<String, CriterionProgress> criteria, CriteriaStrategy strategy) {
        this.criteria = criteria;
        this.criteriaStrategy = strategy;
    }


    public static QuestProgress loadFromCompoundTag(CompoundTag tag) {
        Map<String, CriterionProgress> criteria = new HashMap<>();

        tag.getAllKeys().stream().filter(s -> !s.equals("quest") && !s.equals("strategy")).forEach(s -> criteria.put(s, CriterionProgress.loadFromCompoundTag(tag.getCompound(s))));
        CriteriaStrategy strategy = CriteriaStrategy.valueOf(tag.getString("strategy"));

        return new QuestProgress(criteria, strategy);
    }


    public static QuestProgress fromNetwork(FriendlyByteBuf buf) {
        Map<String, CriterionProgress> map = buf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        CriteriaStrategy strategy = buf.readEnum(CriteriaStrategy.class);
        return new QuestProgress(map, strategy);
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



    public void updateProgress(ProgressionQuest quest) {
        Set<String> set = quest.getCriteria().keySet();
        this.criteria.entrySet().removeIf((entry) -> !set.contains(entry.getKey()));

        for(String s : set) {
            if(!this.criteria.containsKey(s)) {
                this.criteria.put(s, new CriterionProgress());
            }
        }
    }


    @Override
    public float getPercent() {
        if(this.criteria.isEmpty()) {
            return 0.0F;
        }
        else {
            float f = (float) this.criteria.size();
            float f1 = (float) this.countCompletedCriteria();
            return f1 / f;
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


    public int compareTo(QuestProgress questProgress) {
        Date date = this.getFirstProgressDate();
        Date date1 = questProgress.getFirstProgressDate();
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


    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (buf1, criterionProgress) -> criterionProgress.toNetwork(buf1));
        buf.writeEnum(this.criteriaStrategy);
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


    @Override
    public CompoundTag saveToCompoundTag() {
        CompoundTag tag = new CompoundTag();
        this.criteria.forEach((s, criterionProgress) -> tag.put(s, criterionProgress.saveToCompoundTag()));
        tag.putString("strategy", this.criteriaStrategy.name());
        return tag;
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


    @Override
    public void reset() {
        this.criteria.forEach((s, criterionProgress) -> this.revokeProgress(s));
    }


    public Map<String, CriterionProgress> getCriteria() {
        return criteria;
    }


    @Override
    public void complete() {
        this.criteria.forEach((s, criterionProgress) -> this.grantProgress(s));
    }
}
