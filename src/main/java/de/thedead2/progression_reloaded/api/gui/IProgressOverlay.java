package de.thedead2.progression_reloaded.api.gui;

import de.thedead2.progression_reloaded.api.IProgressInfo;
import de.thedead2.progression_reloaded.api.IProgressable;
import net.minecraft.client.gui.components.Renderable;


public interface IProgressOverlay<T extends IProgressable<T>> extends Renderable {

    void updateProgress(IProgressInfo<T> progressInfo);
}
