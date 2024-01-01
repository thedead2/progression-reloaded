package de.thedead2.progression_reloaded.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.helper.CollectionHelper;
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
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2d;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import static de.thedead2.progression_reloaded.client.ModRenderer.isGuiDebug;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_NAME;


@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenEventsListener {

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
            List<String> rightSide = event.getRight();

            if(minecraft.player.isShiftKeyDown()) {
                final PlayerData clientData = ModClientInstance.getInstance().getClientData();
                final int maxQuests = 25;

                if(clientData != null) {
                    rightSide.clear();
                    rightSide.add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + MOD_NAME);
                    rightSide.add("Current Level: " + clientData.getCurrentLevel().getTitle().getString() + " [" + ModHelper.DECIMAL_FORMAT.format(clientData.getCurrentLevelProgress().getPercent() * 100) + " %]");
                    clientData.getTeam().ifPresent(team -> rightSide.add("Team: " + team.getName() + " [" + team.getMembers().size() + " members]"));
                    if(ConfigManager.MAX_EXTRA_LIVES.get() > 0 || ExtraLifeItem.isUnlimited()) {
                        rightSide.add("Extra Lives: " + (ExtraLifeItem.isUnlimited() ? "Unlimited" : clientData.getExtraLives()) + " [max. " + ConfigManager.MAX_EXTRA_LIVES.get() + "]");
                    }

                    List<ProgressionQuest> activeQuests = CollectionHelper.convertCollection(clientData.getQuestData().getStartedOrActiveQuests(), progressionQuest -> progressionQuest);
                    rightSide.add("Active Quests:" + (activeQuests.isEmpty() ? " None" : ""));
                    activeQuests.forEach(quest -> {
                        if(activeQuests.indexOf(quest) < maxQuests) {
                            rightSide.add(quest.getTitle().getString() + " [" + ModHelper.DECIMAL_FORMAT.format(clientData.getQuestData().getOrStartProgress(quest).getPercent() * 100) + " %]");
                        }
                    });
                    if(activeQuests.size() > maxQuests) {
                        rightSide.add("...");
                    }

                    List<ProgressionQuest> completedQuests = CollectionHelper.convertCollection(clientData.getQuestData().getFinishedQuests(), progressionQuest -> progressionQuest);
                    rightSide.add("Finished Quests:" + (completedQuests.isEmpty() ? " None" : ""));
                    completedQuests.forEach(quest -> {
                        if(completedQuests.indexOf(quest) < maxQuests) {
                            rightSide.add(quest.getTitle().getString() + " [" + ModHelper.DECIMAL_FORMAT.format(clientData.getQuestData().getOrStartProgress(quest).getPercent() * 100) + " %]");
                        }
                    });
                    if(completedQuests.size() > maxQuests) {
                        rightSide.add("...");
                    }
                }
            }
            else {
                rightSide.add("");
                rightSide.add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + MOD_NAME + " [Shift]");
            }
        }
    }


    @SubscribeEvent
    public static void onRender(final RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        int mouseX = (int) (minecraft.mouseHandler.xpos() * (double) event.getWindow().getGuiScaledWidth() / (double) event.getWindow().getScreenWidth());
        int mouseY = (int) (minecraft.mouseHandler.ypos() * (double) event.getWindow().getGuiScaledHeight() / (double) event.getWindow().getScreenHeight());
        ModClientInstance.getInstance().getModRenderer().render(event.getPoseStack(), mouseX, mouseY, event.getPartialTick());
    }


    @SubscribeEvent
    public static void onPostScreenRender(final ScreenEvent.Render.Post event) {
        if(isGuiDebug()) {
            Vector2d mousePos = RenderUtil.getMousePos();
            RenderUtil.renderCrossDebug(new PoseStack(), (float) mousePos.x, (float) mousePos.y, 1000000, 5, Color.YELLOW.getRGB());
        }
    }
}

