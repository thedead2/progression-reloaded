package de.thedead2.progression_reloaded.api.criteria;

import de.thedead2.progression_reloaded.api.gui.IDrawHelper;

public interface IField<T> {
    /** Returns the name of the field **/
    public String getFieldName();

    /** Called first when attempting to click **/
    public boolean attemptClick(int mouseX, int mouseY);

    /** Performs a click of attempt click fails and it was clicked in a specific range **/
    public boolean click(int button);

    /** Draw this field **/
    public void draw(IRuleProvider provider, IDrawHelper helper, int renderX, int renderY, int color, int yPos, int mouseX, int mouseY);

    /** Return contents of the field **/
    public T getField();
}
