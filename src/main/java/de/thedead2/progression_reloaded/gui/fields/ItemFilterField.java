package de.thedead2.progression_reloaded.gui.fields;

import de.thedead2.progression_reloaded.api.criteria.IFilter;
import de.thedead2.progression_reloaded.api.criteria.IFilterProvider;
import de.thedead2.progression_reloaded.api.criteria.IFilterType;
import de.thedead2.progression_reloaded.api.criteria.IRuleProvider;
import de.thedead2.progression_reloaded.api.gui.IDrawHelper;
import de.thedead2.progression_reloaded.api.special.IHasFilters;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.api.special.ISetterCallback;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeItem;
import de.thedead2.progression_reloaded.helpers.CollectionHelper;
import net.minecraft.client.gui.GuiScreen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.EDIT;
import static de.thedead2.progression_reloaded.gui.core.GuiList.*;

public class ItemFilterField extends AbstractField {
    private static final Random rand = new Random();
    private IFilterType selector;
    private Field field;

    public ItemFilterField(String fieldName, Object object) {
        super(fieldName);
        this.object = object;

        try {
            field = object.getClass().getField(fieldName);
        } catch (Exception e) {
            try {
                field = object.getClass().getSuperclass().getField(fieldName);
            } catch (Exception e1) {}
        }


        if (object instanceof IHasFilters) {
            selector = ((IHasFilters) object).getFilterForField(getFieldName());
        } else if (object instanceof IFilter) {
            selector = ((IFilter)object).getType();
        } else selector = FilterTypeItem.INSTANCE;
    }

    @Override
    public String getFieldName() {
        return field.getName();
    }

    private transient int ticker = 0;
    private transient String name;

    public IFilterProvider getRandomFilter() {
        List<IFilterProvider> filters = getFilters();
        int size = filters.size();
        if (size == 0) return null;
        if (size == 1) return filters.get(0);
        else {
            return filters.get(rand.nextInt(size));
        }
    }

    @Override
    public String getField() {
        if (ticker %200 == 0) {
            IFilterProvider filter = getRandomFilter();
            if (filter == null) name = null;
            else name = filter.getDescription();
        }

        if (!GuiScreen.isShiftKeyDown()) ticker++;
        return name == null ? "No Valid Filter Set" : name;
    }

    @Override
    public void draw(IRuleProvider provider, IDrawHelper helper, int renderX, int renderY, int color, int yPos, int mouseX, int mouseY) {
        if (MODE == EDIT) {
            helper.drawSplitText(renderX, renderY, de.thedead2.progression_reloaded.ProgressionReloaded.translate("filter." + selector.getName()), 4, yPos, 105, color, 0.75F);
        }
    }

    public boolean isAccepted(IFilterProvider filter) {
        if (filter.getProvided().getType() != selector) return false;
        return true;
    }

    @Override
    public boolean click() {
        try {
            FILTER_EDITOR.setPrevious(CORE.openGui).set(this); //Adjust this filter object
            PREVIEW.select(selector); //Allow for selection of multiple items
            CORE.setEditor(FILTER_EDITOR);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean attemptClick(int mouseX, int mouseY) {
        return false;
    }

    public void setFilters(List<IFilterProvider> filters) {
        try {
            if (object instanceof ISetterCallback) {
                ((ISetterCallback) object).setField(field.getName(), filters);
            } else field.set(object, filters);

            //Init the object after we've set it
            if (object instanceof IInit) {
                ((IInit) object).init(true);
            }

            //Update
            PREVIEW.updateSearch();
        } catch (Exception e) {}
    }

    public List<IFilterProvider> getFilters() {
        try {
            return (List<IFilterProvider>) field.get(object);
        } catch (Exception e) {}

        //Return a blank list yo!
        return new ArrayList();
    }

    public void add(IFilterProvider filter) {
        List<IFilterProvider> filters = getFilters();
        filters.add(filter);
        if (object instanceof ISetterCallback) {
            ((ISetterCallback) object).setField(field.getName(), filters);
        }

        //Call this on init objects
        if (object instanceof IInit) {
            ((IInit) object).init(true);
        }

        //Update
        PREVIEW.updateSearch();
    }

    public void remove(IFilterProvider filter) {
        List<IFilterProvider> filters = getFilters();
        CollectionHelper.remove(filters, filter);
        if (object instanceof ISetterCallback) {
            ((ISetterCallback) object).setField(field.getName(), filters);
        }

        //Call this on init objects
        if (object instanceof IInit) {
            ((IInit) object).init(true);
        }

        //Update
        PREVIEW.updateSearch();
    }
}
