package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.client.gui.ImageRenderInfo;
import net.minecraft.resources.ResourceLocation;

public abstract class ProgressionTheme {
    protected final ImageRenderInfo background;
    protected final ResourceLocation frames;
    protected final ResourceLocation widgets;
    protected ProgressionTheme(ImageRenderInfo background, ResourceLocation frames, ResourceLocation widgets) {
        this.background = background;
        this.frames = frames;
        this.widgets = widgets;
    }

    public ImageRenderInfo getBackground() {
        return background;
    }

    public ResourceLocation getFrames() {
        return frames;
    }

    public ResourceLocation getWidgets() {
        return widgets;
    }

    public void init(int screenWidth, int screenHeight) {

    }
}
