package de.thedead2.progression_reloaded.criteria.rewards;

import de.thedead2.progression_reloaded.api.criteria.IReward;
import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class RewardBase implements IReward {
    private IRewardProvider provider;

    @Override
    public void setProvider(IRewardProvider provider) {
        this.provider = provider;
    }

    @Override
    public IRewardProvider getProvider() {
        return provider;
    }

    @Override
    public void onAdded(boolean isClient) {}

    @Override
    public void onRemoved() {}

    @Override
    public boolean shouldRunOnce() {
        return false;
    }
    
    @Override
    public void reward(EntityPlayerMP player) {}

    protected String translate(String text) {
        return de.thedead2.progression_reloaded.ProgressionReloaded.translate(provider.getUnlocalisedName() + ".description." + text);
    }

    protected String format(Object... data) {
        return format(null, data);
    }

    protected String format(String text, Object... data) {
        return text == null ? de.thedead2.progression_reloaded.ProgressionReloaded.format(provider.getUnlocalisedName() + ".description", data): de.thedead2.progression_reloaded.ProgressionReloaded.format(provider.getUnlocalisedName() + ".description." + text, data);
    }
}
