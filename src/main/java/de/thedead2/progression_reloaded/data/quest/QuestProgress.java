package de.thedead2.progression_reloaded.data.quest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.thedead2.progression_reloaded.data.criteria.CriteriaStrategy;
import de.thedead2.progression_reloaded.data.criteria.CriterionProgress;
import de.thedead2.progression_reloaded.data.criteria.ICriterion;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestProgress implements Comparable<QuestProgress>{
    final Map<String, CriterionProgress> criteria;
    private CriteriaStrategy criteriaStrategy;

    private QuestProgress(Map<String, CriterionProgress> pCriteria) {
        this.criteria = pCriteria;
    }

    public QuestProgress() {
        this.criteria = Maps.newHashMap();
    }


    public void update(Map<String, ICriterion> pCriteria, CriteriaStrategy criteriaStrategy) {
        Set<String> set = pCriteria.keySet();
        this.criteria.entrySet().removeIf((entry) -> !set.contains(entry.getKey()));

        for(String s : set) {
            if (!this.criteria.containsKey(s)) {
                this.criteria.put(s, new CriterionProgress());
            }
        }

        this.criteriaStrategy = criteriaStrategy;
    }

    public boolean isDone() {
        boolean flag = false;
        for(String s : criteria.keySet()){
            CriterionProgress criterionprogress = this.getCriterion(s);
            if (criterionprogress != null && criterionprogress.isDone()) {
                flag = true;
                if(criteriaStrategy.equals(CriteriaStrategy.OR)) break;
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

    public boolean grantProgress(String criterionName) {
        CriterionProgress criterionprogress = this.criteria.get(criterionName);
        if (criterionprogress != null && !criterionprogress.isDone()) {
            criterionprogress.grant();
            return true;
        } else {
            return false;
        }
    }

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

    public static QuestProgress fromNetwork(FriendlyByteBuf buf) {
        Map<String, CriterionProgress> map = buf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        return new QuestProgress(map);
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
}
