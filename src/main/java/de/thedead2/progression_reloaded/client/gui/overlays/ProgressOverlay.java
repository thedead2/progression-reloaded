package de.thedead2.progression_reloaded.client.gui.overlays;

import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.IProgressOverlay;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.client.gui.Font;


public abstract class ProgressOverlay implements IProgressOverlay {

    protected final Area area;

    protected final ProgressBar progressBar;

    protected final DrawableTexture backgroundFrame;

    protected final Font font;

    protected IDisplayInfo displayInfo;


    public ProgressOverlay(Area area, IDisplayInfo displayInfo, ProgressBar progressBar, TextureInfo backgroundFrame, Font font) {
        this.area = area;
        this.displayInfo = displayInfo;
        this.progressBar = progressBar;
        this.backgroundFrame = new DrawableTexture(backgroundFrame, this.area);
        this.font = font;
    }


    @Override
    public void updateProgress(Class<? extends IProgressOverlay> target, IProgressInfo progressInfo) {
        if(this.getClass().equals(target)) {
            this.progressBar.updateProgress(progressInfo);
        }
    }


    @Override
    public void updateDisplayInfo(Class<? extends IProgressOverlay> target, IDisplayInfo displayInfo) {
        if(this.getClass().equals(target)) {
            this.displayInfo = displayInfo;
        }
    }
}
