package de.thedead2.progression_reloaded.data.criteria;

import de.thedead2.progression_reloaded.data.quest.QuestProgress;

public enum CriteriaStrategy {
    OR {
        @Override
        public boolean isDone(QuestProgress questProgress) {
            for(String s : questProgress.getCriteria().keySet()) {
                CriterionProgress criterionprogress = questProgress.getCriterion(s);
                if (criterionprogress != null && criterionprogress.isDone()) {
                    return true;
                }
            }
            return false;
        }
    },
    AND {
        @Override
        public boolean isDone(QuestProgress questProgress) {
            boolean flag = false;
            for(String s : questProgress.getCriteria().keySet()){
                CriterionProgress criterionprogress = questProgress.getCriterion(s);
                if (criterionprogress != null && criterionprogress.isDone()) {
                    flag = true;
                }
                else {
                    flag = false;
                    break;
                }
            }

            return flag;
        }
    };

    public abstract boolean isDone(QuestProgress questProgress);
}
