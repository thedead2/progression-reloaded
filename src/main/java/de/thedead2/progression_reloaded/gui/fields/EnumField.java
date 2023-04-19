package de.thedead2.progression_reloaded.gui.fields;

import de.thedead2.progression_reloaded.api.criteria.IRuleProvider;
import de.thedead2.progression_reloaded.api.gui.IDrawHelper;
import de.thedead2.progression_reloaded.api.special.IEnum;
import de.thedead2.progression_reloaded.api.special.IInit;

import java.lang.reflect.Field;

public class EnumField extends AbstractField {
    public Field field;
    public IEnum object;

    public EnumField(String displayName, String fieldName, IEnum object) {
        super(displayName);
        this.object = object;

        try {
            field = object.getClass().getField(fieldName);
        } catch (Exception e) {}
    }

    public EnumField(String name, IEnum object) {
        this(name, name, object);
    }

    public Enum getName() throws IllegalArgumentException, IllegalAccessException {
        return (Enum) field.get(object);
    }

    public void setField(Object next) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, next);

        //Init the object after we've set it
        if (object instanceof IInit) {
            ((IInit) object).init(true);
        }
    }

    @Override
    public String getFieldName() {
        return field.getName();
    }

    @Override
    public boolean click() {
        try {
            setField(object.next(getFieldName()));
            return true;
        } catch (Exception e) { return false;}
    }

    @Override
    public String getField() {
        try {
            return getName().toString().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void draw(IRuleProvider provider, IDrawHelper helper, int renderX, int renderY, int color, int yPos, int mouseX, int mouseY) {
        try {
            String value = getName().toString().toLowerCase();
            helper.drawSplitText(renderX, renderY, name + ": " + value, 4, yPos, 150, color, 0.75F);
        } catch (Exception e) {}
    }

    @Override
    public void setObject(Object object) {
        this.object = (IEnum) object;
    }
}