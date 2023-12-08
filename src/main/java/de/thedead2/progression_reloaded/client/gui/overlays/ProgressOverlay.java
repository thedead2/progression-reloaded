package de.thedead2.progression_reloaded.client.gui.overlays;

import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.api.IProgressable;
import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.gui.IProgressOverlay;
import de.thedead2.progression_reloaded.client.gui.components.ProgressBar;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import de.thedead2.progression_reloaded.client.gui.textures.DrawableTexture;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.resources.ResourceLocation;


public abstract class ProgressOverlay<T extends IProgressable<T>> implements IProgressOverlay<T> {

    protected final Area area;

    protected final ProgressBar<T> progressBar;

    protected final DrawableTexture backgroundFrame;

    protected final ProgressionFont font;

    protected IDisplayInfo<T> displayInfo;


    public ProgressOverlay(Area area, IDisplayInfo<T> displayInfo, ProgressBar<T> progressBar, TextureInfo backgroundFrame, ResourceLocation font) {
        this.area = area;
        this.displayInfo = displayInfo;
        this.progressBar = progressBar;
        this.backgroundFrame = backgroundFrame != null ? new DrawableTexture(backgroundFrame, this.area) : null;
        this.font = FontManager.getFont(font);
    }


    @Override
    public void updateProgress(IProgressInfo<T> progressInfo) {
        this.progressBar.updateProgress(progressInfo);
    }


    @Override
    public void updateDisplayInfo(IDisplayInfo<T> displayInfo) {
        this.displayInfo = displayInfo;
    }
}
