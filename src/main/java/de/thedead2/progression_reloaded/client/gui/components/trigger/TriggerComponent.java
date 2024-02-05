package de.thedead2.progression_reloaded.client.gui.components.trigger;

import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.trigger.SimpleCriterionTrigger;


public abstract class TriggerComponent<T extends SimpleCriterionTrigger<?>> extends ScreenComponent {

    protected final T trigger;


    public TriggerComponent(Area area, T trigger) {
        super(area);
        this.trigger = trigger;
    }
}
