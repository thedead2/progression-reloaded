package de.thedead2.progression_reloaded.client.gui.components.rewards;

import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import de.thedead2.progression_reloaded.data.rewards.IReward;


public abstract class RewardComponent<T extends IReward> extends ScreenComponent {

    protected final T reward;


    public RewardComponent(Area area, T reward) {
        super(area);
        this.reward = reward;
    }
}
