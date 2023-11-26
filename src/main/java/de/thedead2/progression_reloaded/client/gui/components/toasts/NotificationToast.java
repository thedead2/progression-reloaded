package de.thedead2.progression_reloaded.client.gui.components.toasts;

import de.thedead2.progression_reloaded.api.gui.animation.IAnimation;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Alignment;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public abstract class NotificationToast extends ScreenComponent {

    protected final Component title;

    protected final Alignment toastAlignment;

    protected final IAnimation animation;


    protected NotificationToast(Area area, Component title, Alignment toastAlignment, IAnimation animation) {
        super(area);
        this.title = title;
        this.toastAlignment = toastAlignment;
        this.animation = animation;
        this.setFocused(true);
    }


    public final boolean shouldRender() {
        return !this.animation.isFinishedAndNotLooping();
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.title);
    }


    public final void pauseAnimation() {
        this.animation.pause(true);
    }


    public final void resumeAnimation() {
        this.animation.pause(false);
    }


    public enum Priority {
        HIGH,
        LOW
    }
}
