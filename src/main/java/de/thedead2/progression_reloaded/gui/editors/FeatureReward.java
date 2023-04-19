package de.thedead2.progression_reloaded.gui.editors;

import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.api.criteria.IRewardProvider;
import de.thedead2.progression_reloaded.api.criteria.ITriggerProvider;
import de.thedead2.progression_reloaded.gui.core.GuiList;
import de.thedead2.progression_reloaded.helpers.CollectionHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketSelectRewards;
import de.thedead2.progression_reloaded.player.PlayerTracker;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;
import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class FeatureReward extends FeatureDrawable<IRewardProvider> {
    private Set<IRewardProvider> selected;

    public FeatureReward() {
        super("reward", 140, NEW_REWARD, THEME.rewardBoxGradient1, THEME.rewardBoxGradient2, THEME.rewardBoxFont, THEME.rewardBoxGradient2);
    }

    public void reset(ICriteria criteria) {
        if (selected == null) {
            selected = new LinkedHashSet<IRewardProvider>();
        } else {
            if (selected.size() > 0) {
                IRewardProvider first = null;
                for (IRewardProvider reward: selected) {
                    first = reward;
                    break;
                }

                //If we are a different criteria, then reset everything
                if (!first.getCriteria().getUniqueID().equals(criteria.getUniqueID())) {
                    selected = new LinkedHashSet<IRewardProvider>();
                }
            }
        }
    }

    @Override
    public boolean isReady() {
        return CRITERIA_EDITOR.get() != null;
    }

    @Override
    public List<IRewardProvider> getList() {
        return CRITERIA_EDITOR.get().getRewards();
    }

    public Set<IRewardProvider> getSelected() {
        if (selected == null) {
            selected = new LinkedHashSet<IRewardProvider>();
        }

        return selected;
    }

    @Override
    public int drawSpecial(IRewardProvider drawing, int offsetX, int offsetY, int mouseOffsetX, int mouseOffsetY) {
        boolean allTrue = true;
        for (ITriggerProvider provider: drawing.getCriteria().getTriggers()) {
            if (!provider.getProvided().isCompleted()) allTrue = false;
        }

        if (allTrue) {
            if (selected.contains(drawing) || !drawing.mustClaim()) {
                offset.drawGradient(offsetX, offsetY, 1, 2, drawing.getWidth(GuiList.MODE) - 1, 75, 0x33222222, 0x00CCCCCC, 0x00000000);
            }
        }

        return super.drawSpecial(drawing, offsetX, offsetY, mouseOffsetX, mouseOffsetY);
    }

    @Override
    public boolean clickSpecial(IRewardProvider provider, int mouseOffsetX, int mouseOffsetY) {
        if (MODE == EDIT) return false; //Don't you dare!

        if (mouseOffsetY < 2 || mouseOffsetY > 73) return false;
        for (ITriggerProvider trigger: provider.getCriteria().getTriggers()) {
            if (!trigger.getProvided().isCompleted()) return false;
        }

        UUID uuid = provider.isOnePerTeam() ? PlayerTracker.getClientPlayer().getTeam().getOwner() : PlayerHelper.getClientUUID();
        if (!PlayerTracker.getClientPlayer().getMappings().getUnclaimedRewards(uuid).contains(provider)) return false;
        if (mouseOffsetX > 0 && mouseOffsetX < provider.getWidth(MODE) && provider.mustClaim()) {
            if (select(provider)) sendToServer();
            return true;
        }

        return false;
    }

    public void sendToServer() {
        PacketHandler.sendToServer(new PacketSelectRewards(selected));
        selected = new LinkedHashSet<IRewardProvider>(); //Reset the hashset
    }

    public boolean isSelected(IRewardProvider provider) {
        return selected == null? false: selected.contains(provider);
    }

    public boolean select(IRewardProvider provider) {
        return select(provider, false);
    }

    public boolean select(IRewardProvider provider, boolean simulate) {
        if (selected == null) return false; //You are not allowed to be visible if selections can't even happen
        //Click processed as this item must be claimed, now we check the side of selected, vs other things
        if (provider != null && !simulate) {
            if (selected.contains(provider)) {
                CollectionHelper.remove(selected, provider);
                return false;
            } //If we already had it, screw validation
        }

        int standard = 0;
        ICriteria criteria = provider.getCriteria();
        int maximum = criteria.givesAllRewards() ? criteria.getRewards().size() : criteria.getAmountOfRewards();
        for (IRewardProvider reward : criteria.getRewards()) {
            if (!reward.mustClaim()) standard++;
        }

        int current = selected.size() + standard;
        if (current < maximum && !simulate) {
            selected.add(provider);
            current++;
        }

        if (current >= maximum) {
            return true;
        }

        return false;
    }
}
