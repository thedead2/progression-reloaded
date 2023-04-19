package de.thedead2.progression_reloaded.criteria.conditions;

import de.thedead2.progression_reloaded.api.IPlayerTeam;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.IGetterCallback;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.handlers.APICache;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

@ProgressionRule(name="criteria", meta="ifCriteriaCompleted")
public class ConditionHasCriteria extends ConditionBase implements IInit, ICustomDescription, IGetterCallback {
    private ICriteria criteria = null;
    private UUID criteriaID = UUID.randomUUID();
    public String displayName = "";

    @Override
    public void init(boolean isClient) {
        try {
            for (ICriteria c : APICache.getCache(isClient).getCriteriaSet()) {
                String display = c.getLocalisedName();
                if (c.getLocalisedName().equals(displayName)) {
                    criteria = c;
                    criteriaID = c.getUniqueID();
                    break;
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public String getDescription() {
        if (criteria != null) {
            if (getProvider().isInverted()) return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description.inverted", criteria.getLocalisedName());
            else return de.thedead2.progression_reloaded.ProgressionReloaded.format(getProvider().getUnlocalisedName() + ".description", criteria.getLocalisedName());
        } else return "BROKEN CRITERIA";
    }

    @Override
    public String getField(String fieldName) {
        return criteria != null ? TextFormatting.GREEN + displayName : TextFormatting.RED + displayName;
    }


    @Override
    public boolean isSatisfied(IPlayerTeam team) {
        if (criteria != null) {
            return PlayerTracker.getServerPlayer(team.getOwner()).getMappings().getCompletedCriteria().keySet().contains(criteria);
        }

        return false;
    }
}
