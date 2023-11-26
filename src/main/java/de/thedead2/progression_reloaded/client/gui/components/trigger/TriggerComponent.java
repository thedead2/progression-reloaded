package de.thedead2.progression_reloaded.client.gui.components.trigger;

import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.trigger.SimpleTrigger;


public abstract class TriggerComponent<T extends SimpleTrigger<?>> extends ScreenComponent {

    protected final T trigger;


    public TriggerComponent(Area area, T trigger) {
        super(area);
        this.trigger = trigger;
    }
}
