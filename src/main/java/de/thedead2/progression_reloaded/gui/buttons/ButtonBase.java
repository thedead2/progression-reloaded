package de.thedead2.progression_reloaded.gui.buttons;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static de.thedead2.progression_reloaded.gui.core.GuiList.CORE;

public abstract class ButtonBase extends GuiButton {
    protected boolean isSideways;

    public ButtonBase(int id, int xPosition, int yPosition, int width, int height, String name) {
        super(id, xPosition, yPosition, width, height, name);
    }

    public ButtonBase setSideways() {
        this.isSideways = true;
        this.height = 23;
        return this;
    }


    @Override
    public boolean mousePressed(Minecraft mc, int x, int y) {
        if (isSideways) x = x - CORE.getOffsetX();
        if (super.mousePressed(mc, x, y)) {
            onClicked();
            return true;
        } else {
            onNotClicked();
            return false;
        }
    }

    public abstract void onClicked();
    
    public void onNotClicked() {
        
    }
}