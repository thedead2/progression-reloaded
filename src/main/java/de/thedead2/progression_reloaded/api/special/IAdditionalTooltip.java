package de.thedead2.progression_reloaded.api.special;

import java.util.List;

/** T is normally an entity or an itemstack **/
public interface IAdditionalTooltip<T> {
    public void addHoverTooltip(String field, T object, List<String> tooltip);
}
