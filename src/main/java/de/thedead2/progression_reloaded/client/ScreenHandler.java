package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.player.PlayerDataHandler;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_NAME;

@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    @SubscribeEvent
    public static void onF3(CustomizeGuiOverlayEvent.DebugText event) {
        final Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.renderDebug) {

            if (minecraft.player.isShiftKeyDown()) {

                final SinglePlayer data = PlayerDataHandler.getActivePlayer(minecraft.player);
                final int maxQuests = 5;

                if (data != null) {
                    event.getRight().add("");
                    event.getRight().add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + MOD_NAME);
                    event.getRight().add("Level: " + data.getProgressionLevel().getName());
                    data.getTeam().ifPresent(team -> event.getRight().add("Team: " + team.getName()));
                    event.getRight().add("Active Quests:");
                    var list = LevelManager.getInstance().getQuestManager().getActiveQuests(KnownPlayer. fromSinglePlayer(data))
                            .stream()
                            .map(ProgressionQuest::getName)
                            .toList();
                    list.forEach(s -> {
                        if(list.indexOf(s) < maxQuests) event.getRight().add(s);
                    });
                    if(list.size() > maxQuests) event.getRight().add("...");
                    else if(list.isEmpty()) event.getRight().add("None");
                }
            }

            else {
                event.getRight().add("");
                event.getRight().add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + MOD_NAME + " [Shift]");
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

