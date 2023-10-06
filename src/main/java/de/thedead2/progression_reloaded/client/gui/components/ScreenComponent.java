package de.thedead2.progression_reloaded.client.gui.components;

import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Renderable;


public abstract class ScreenComponent extends GuiComponent implements Renderable {

    protected final Area area;


    protected ScreenComponent(Area area) {
        this.area = area;
    }


    public Area getArea() {
        return area;
    }
}
