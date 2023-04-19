package de.thedead2.progression_reloaded.api.criteria;

import de.thedead2.progression_reloaded.api.IPlayerTeam;

public interface ICondition extends IRule<IConditionProvider> {
    /** Should true true if this condition is satisfied
     * @param team this is information about the team **/
    public boolean isSatisfied(IPlayerTeam team);
}
