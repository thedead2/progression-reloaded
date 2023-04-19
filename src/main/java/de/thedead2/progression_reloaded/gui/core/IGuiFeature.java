package de.thedead2.progression_reloaded.gui.core;

public interface IGuiFeature {
    public IGuiFeature init();
    public void draw(int mouseX, int mouseY);
    public boolean mouseClicked(int mouseX, int mouseY, int button);
    public boolean isVisible();
    public boolean scroll(int mouseX, int mouseY, boolean down);
    public void setVisibility(boolean isVisible);
    public boolean isOverlay();
}
