package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.client.data.ClientPlayer;
import de.thedead2.progression_reloaded.client.data.ClientQuest;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_NAME;


@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenHandler {

    @SubscribeEvent
    public static void afterScreenInit(final ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if(screen instanceof PauseScreen pauseScreen) {
            if(!pauseScreen.showPauseMenu) {
                return;
            }
            if(ConfigManager.DISABLE_ADVANCEMENTS.get()) {
                findButton(event.getListenersList(), "gui.advancements").ifPresentOrElse(event::removeListener, () -> {
                    for(GuiEventListener eventListener : event.getListenersList()) {
                        if(eventListener instanceof GridWidget gridWidget) {
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


    private static Optional<Button> findButton(List<? extends GuiEventListener> listeners, String name) {
        for(GuiEventListener listener : listeners) {
            if(listener instanceof Button button && button.getMessage() instanceof MutableComponent mutableComponent && mutableComponent.getContents() instanceof TranslatableContents translatableContents) {
                if(translatableContents.getKey().equals(name)) {
                    return Optional.of(button);
                }
            }
        }
        return Optional.empty();
    }


    private static Optional<Button> findButtonInGrid(GridWidget gridWidget, String name) {
        return findButton(gridWidget.children(), name);
    }


    @SubscribeEvent
    public static void beforeScreenInit(final ScreenEvent.Init.Pre event) {
        if(event.getScreen() instanceof AdvancementsScreen) {
            if(ConfigManager.DISABLE_ADVANCEMENTS.get()) {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }


    @SubscribeEvent
    public static void onF3(final CustomizeGuiOverlayEvent.DebugText event) {
        final Minecraft minecraft = Minecraft.getInstance();

        if(minecraft.options.renderDebug) {

            if(minecraft.player.isShiftKeyDown()) {

                final ClientPlayer data = ClientManager.getInstance().getPlayer();
                final int maxQuests = 5;

                if(data != null) {
                    event.getRight().add("");
                    event.getRight().add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + MOD_NAME);
                    if(ConfigManager.MAX_EXTRA_LIVES.get() > 0) {
                        event.getRight().add("Extra Lives: " + data.getExtraLives());
                    }
                    event.getRight().add("Level: " + data.getLevels().getCurrent().getTitle().getString());
                    data.getTeam().ifPresent(team -> event.getRight().add("Team: " + team.getName()));
                    event.getRight().add("Active Quests:");
                    var list = data.getQuests()
                                   .getActiveQuests()
                                   .stream()
                                   .map(ClientQuest::getTitle)
                                   .map(Component::getString)
                                   .toList();
                    list.forEach(s -> {
                        if(list.indexOf(s) < maxQuests) {
                            event.getRight().add(s);
                        }
                    });
                    if(list.size() > maxQuests) {
                        event.getRight().add("...");
                    }
                    else if(list.isEmpty()) {
                        event.getRight().add("None");
                    }
                }
            }

            else {
                event.getRight().add("");
                event.getRight().add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + MOD_NAME + " [Shift]");
            }
        }
    }


    @SubscribeEvent
    public static void onRender(final RenderGuiEvent.Pre event) {
        RenderUtil.renderExtraLifeAnimation(event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), event.getPartialTick());
    }
}

