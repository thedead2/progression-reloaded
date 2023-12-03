package de.thedead2.progression_reloaded.client.gui.themes.layouts;

import com.google.common.base.Preconditions;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.client.gui.util.Padding;


public record ProgressionLayout(Area toast, Area levelProgressOL, Area questProgressOL) {


    public static class Builder {

        private Area toast, levelProgressOL, questProgressOL;


        private Builder() {}


        public static Builder builder() {
            return new Builder();
        }


        public Builder withToast(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
            this.toast = new Area(xPos, yPos, zPos, width, height, padding);

            return this;
        }


        public Builder withLevelProgressOL(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
            this.levelProgressOL = new Area(xPos, yPos, zPos, width, height, padding);

            return this;
        }


        public Builder withQuestProgressOL(float xPos, float yPos, float zPos, float width, float height, Padding padding) {
            this.questProgressOL = new Area(xPos, yPos, zPos, width, height, padding);

            return this;
        }


        public ProgressionLayout build() {
            Preconditions.checkNotNull(toast);
            Preconditions.checkNotNull(levelProgressOL);
            Preconditions.checkNotNull(questProgressOL);

            return new ProgressionLayout(toast, levelProgressOL, questProgressOL);
        }
    }
}
