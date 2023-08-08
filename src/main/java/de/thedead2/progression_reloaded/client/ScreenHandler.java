package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenHandler {
    @SubscribeEvent
    public static void afterScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if(screen instanceof PauseScreen pauseScreen){
            if(!pauseScreen.showPauseMenu) return;
            if (ConfigManager.DISABLE_ADVANCEMENTS.get()) {
                findButton(event.getListenersList(), "gui.advancements").ifPresentOrElse(event::removeListener, () -> {
                    for (GuiEventListener eventListener : event.getListenersList()) {
                        if (eventListener instanceof GridWidget gridWidget) {
                            findButtonInGrid(gridWidget, "gui.advancements").ifPresent(button -> {
                                List<? extends GuiEventListener> children = gridWidget.children();
                                children.remove(button);
                                findButtonInGrid(gridWidget, "gui.stats").ifPresent(button1 -> {
                                    button1.setWidth(204);
                                    button1.setX(button1.getX() - (204 / 2 + 4));
                                });
                            });
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void beforeScreenInit(ScreenEvent.Init.Pre event){
        if (event.getScreen() instanceof AdvancementsScreen) {
            if(ConfigManager.DISABLE_ADVANCEMENTS.get()){
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    private static Optional<Button> findButton(List<? extends GuiEventListener> listeners, String name) {
        for (GuiEventListener listener : listeners) {
            if (listener instanceof Button button && button.getMessage() instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContents) {
                if (translatableContents.getKey().equals(name))
                    return Optional.of(button);
            }
        }
        return Optional.empty();
    }

    private static Optional<Button> findButtonInGrid(GridWidget gridWidget, String name){
        return findButton(gridWidget.children(), name);
    }
}

