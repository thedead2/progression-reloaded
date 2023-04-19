package de.thedead2.progression_reloaded.progression_reloaded.util.exceptions;

import java.util.UUID;

public class CriteriaNotFoundException extends NullPointerException {
    public CriteriaNotFoundException(UUID name) {
        super("The following criteria does not actually exist: " + name);
    }
}
