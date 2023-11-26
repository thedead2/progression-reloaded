package de.thedead2.progression_reloaded.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.toasts.NotificationToast;
import de.thedead2.progression_reloaded.util.ModHelper;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;


public class NotificationToastRenderer {

    private static final int maxSize = 10; //TODO: Add config option

    private static final boolean displayToasts = true;

    private final LinkedBlockingDeque<NotificationToast> toasts = new LinkedBlockingDeque<>(maxSize);

    private final Queue<NotificationToast> toastQueue = new PriorityQueue<>(maxSize);


    public void renderToastsIfNeeded(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if(!this.toasts.isEmpty()) {
            NotificationToast toast = this.toasts.peekFirst();
            if(!ModHelper.isGamePaused()) {
                if(toast.shouldRender()) {
                    toast.resumeAnimation();
                    toast.render(poseStack, mouseX, mouseY, partialTick);
                }
                else {
                    this.toasts.removeFirst();
                    this.insertHighPriorityToasts();
                }
            }
            else {
                toast.pauseAnimation();
            }
        }
        else {
            this.insertHighPriorityToasts();
        }
    }


    private void insertHighPriorityToasts() {
        for(NotificationToast toast : this.toastQueue) {
            this.toasts.offerFirst(toast);
        }
        this.toastQueue.clear();
    }


    public void scheduleToastDisplay(NotificationToast.Priority priority, NotificationToast toast) {
        if(displayToasts) {
            switch(priority) {
                case HIGH -> this.toastQueue.offer(toast);
                case LOW -> this.toasts.offerLast(toast);
            }
        }
    }


    public void forceDisplayToast(NotificationToast toast) {
        this.toasts.pollFirst();
        this.toasts.addFirst(toast);
    }
}
