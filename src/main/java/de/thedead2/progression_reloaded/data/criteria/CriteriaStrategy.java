package de.thedead2.progression_reloaded.data.criteria;

import de.thedead2.progression_reloaded.data.quest.QuestTasks;


public enum CriteriaStrategy {
    OR {
        @Override
        public boolean isDone(QuestTasks.TaskProgress taskProgress) {
            for(String s : taskProgress.getCriteria().keySet()) {
                CriterionProgress criterionprogress = taskProgress.getCriterion(s);
                if(criterionprogress != null && criterionprogress.isDone()) {
                    return true;
                }
            }
            return false;
        }
    },
    AND {
        @Override
        public boolean isDone(QuestTasks.TaskProgress taskProgress) {
            boolean flag = false;
            for(String s : taskProgress.getCriteria().keySet()) {
                CriterionProgress criterionprogress = taskProgress.getCriterion(s);
                if(criterionprogress != null && criterionprogress.isDone()) {
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


    public abstract boolean isDone(QuestTasks.TaskProgress taskProgress);
}
