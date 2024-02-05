package de.thedead2.progression_reloaded.data.tasks;

import net.minecraft.network.chat.Component;

import java.util.Collection;


public enum TaskStrategy {
    OR {
        @Override
        public boolean isDone(Collection<TaskProgress> taskProgresses) {
            for(TaskProgress taskProgress : taskProgresses) {
                if(taskProgress.isDone() && !taskProgress.isOptional()) {
                    return true;
                }
            }
            return false;
        }


        @Override
        public Component getDefaultDescription() {
            return Component.literal(" or ");
        }
    },
    AND {
        @Override
        public boolean isDone(Collection<TaskProgress> taskProgresses) {
            boolean flag = false;
            for(TaskProgress taskProgress : taskProgresses) {
                flag = taskProgress.isDone() || taskProgress.isOptional();

                if(!flag) {
                    break;
                }
            }

            return flag;
        }


        @Override
        public Component getDefaultDescription() {
            return Component.literal(" and ");
        }
    };


    public abstract boolean isDone(Collection<TaskProgress> taskProgresses);

    public abstract Component getDefaultDescription();
}
