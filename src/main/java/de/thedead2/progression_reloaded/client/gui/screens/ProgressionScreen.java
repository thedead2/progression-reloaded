package de.thedead2.progression_reloaded.client.gui.screens;

import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.client.gui.fonts.types.ProgressionFont;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;


public abstract class ProgressionScreen extends Screen {

    @Nullable
    protected final Screen parent;

    protected final ProgressionFont font;


    protected ProgressionScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parent = parent;
        this.font = FontManager.getInstance().getFont(ModClientInstance.getInstance().getModRenderer().getThemeManager().getActiveTheme().get().font());
    }


    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);

        for(GuiEventListener eventListener : this.children()) {
            eventListener.changeFocus(false);
        }
        if(listener != null) {
            listener.changeFocus(true);
        }
    }


    public void setBackgroundBlur() {
        this.minecraft.gameRenderer.loadEffect(new ResourceLocation("shaders/post/blur.json"));
    }


    public void resetBlurEffect() {
        this.minecraft.gameRenderer.shutdownEffect();
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
