package de.thedead2.progression_reloaded.api.gui;

import de.thedead2.progression_reloaded.api.IProgressInfo;
import net.minecraft.client.gui.components.Renderable;


public interface IProgressOverlay extends Renderable {

    void updateProgress(Class<? extends IProgressOverlay> target, IProgressInfo progressInfo);

    void updateDisplayInfo(Class<? extends IProgressOverlay> target, IDisplayInfo displayInfo);
}
