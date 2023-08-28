package de.thedead2.progression_reloaded.client.gui.themes.layouts;

import de.thedead2.progression_reloaded.client.gui.util.Area;

import java.util.HashMap;
import java.util.Map;


public abstract class ThemeLayout {

    private final Area triggerArea, rewardsArea, descriptionArea, titleArea, iconArea, questsArea, levelsArea, toastArea;


    public ThemeLayout(Area triggerArea, Area rewardsArea, Area descriptionArea, Area titleArea, Area iconArea, Area questsArea, Area levelsArea, Area toastArea) {
        Map<String, Area> areas = new HashMap<>();
        this.triggerArea = areas.computeIfAbsent("triggerArea", s -> triggerArea);
        this.rewardsArea = areas.computeIfAbsent("rewardsArea", s -> rewardsArea);
        this.descriptionArea = areas.computeIfAbsent("descriptionArea", s -> descriptionArea);
        this.titleArea = areas.computeIfAbsent("titleArea", s -> titleArea);
        this.iconArea = areas.computeIfAbsent("iconArea", s -> iconArea);
        this.questsArea = areas.computeIfAbsent("questsArea", s -> questsArea);
        this.levelsArea = areas.computeIfAbsent("levelsArea", s -> levelsArea);

        this.toastArea = areas.computeIfAbsent("toastArea", s -> toastArea);

        this.checkAreas(areas);
    }


    private void checkAreas(Map<String, Area> areas) {
        areas.forEach((s_check, checkArea) -> areas.forEach((s_test, testArea) -> {
            if(!(s_check.equals(s_test) && checkArea.equals(testArea)) && checkArea.clashesWith(testArea)) {
                throw new LayoutException("Area " + s_check + " clashes with area " + s_test);
            }
        }));
    }


    private boolean checkIfOccupied(Area checkArea, Area testArea) {
        return checkArea.clashesWith(testArea);
    }


    private static class LayoutException extends RuntimeException {

        public LayoutException() {

        }


        public LayoutException(String message) {
            super(message);
        }
    }
}
