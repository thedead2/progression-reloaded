package de.thedead2.progression_reloaded.data.abilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class ListAbility<T> implements IAbility<T>{
    protected final Set<T> usable = new HashSet<>();
    protected final boolean blacklist;

    protected ListAbility(boolean blacklist, Collection<T> usable) {
        this.blacklist = blacklist;
        this.usable.addAll(usable);
    }

    @Override
    public boolean isPlayerAbleTo(T t) {
        return blacklist == usable.contains(t) ;
    }
}
