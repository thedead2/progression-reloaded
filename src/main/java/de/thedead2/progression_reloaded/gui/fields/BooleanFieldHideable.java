package de.thedead2.progression_reloaded.gui.fields;

import de.thedead2.progression_reloaded.api.special.IHideable;

public class BooleanFieldHideable extends BooleanField implements IHideable {
    public BooleanFieldHideable(String name, Object object) {
        super(name, object);
    }

    @Override
    public String getField() {
        return isVisible() ? de.thedead2.progression_reloaded.ProgressionReloaded.translate("hideable." + name) : "";
    }

    @Override
    public boolean click() {
        if (isVisible()) {
            return  super.click();
        }

        return false;
    }

    @Override
    public boolean isVisible() {
        boolean result = false;
        try {
            result = getBoolean();
        } catch (Exception e) {}
        
        return result == true;
    }
}
