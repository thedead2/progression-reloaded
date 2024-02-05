package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import de.thedead2.progression_reloaded.client.gui.components.toasts.NotificationToast;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.player.data.PlayerQuests;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientOnProgressChangedPacket implements ModNetworkPacket {

    private final IDisplayInfo<?> displayInfo;

    private final Type title;


    public ClientOnProgressChangedPacket(IDisplayInfo<?> displayInfo, Type title) {
        this.displayInfo = displayInfo;
        this.title = title;
    }


    @SuppressWarnings("unused")
    public ClientOnProgressChangedPacket(FriendlyByteBuf buf) {
        this.displayInfo = IDisplayInfo.deserializeFromNetwork(buf);
        this.title = buf.readEnum(Type.class);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                PlayerData clientData = ModClientInstance.getInstance().getClientData();
                PlayerQuests playerQuests = clientData.getPlayerQuests();
                ModRenderer renderer = ModClientInstance.getInstance().getModRenderer();

                if(title != Type.LOGIN && (title != Type.QUEST_UPDATED || !renderer.isQuestFollowed(displayInfo.id()))) {
                    renderer.getToastRenderer().scheduleToastDisplay(NotificationToast.Priority.LOW, GuiFactory.createProgressToast(displayInfo, title));
                }

                switch(title) {
                    case LEVEL_COMPLETE: {
                        renderer.updateLevelProgressOverlay(clientData.getCurrentLevelProgress());
                        renderer.displayNewLevelInfoScreen(clientData.getCurrentLevel().getId());
                        break;
                    }
                    case QUEST_UPDATED: {
                        if(renderer.isQuestFollowed(displayInfo.id())) {
                            renderer.updateQuestProgressOverlay(playerQuests.getOrStartProgress(displayInfo.id()));
                        }
                        break;
                    }
                    case QUEST_COMPLETE:
                    case QUEST_FAILED: {
                        playerQuests.getStartedOrActiveQuests()
                                    .stream()
                                    .filter(ProgressionQuest::isMainQuest)
                                    .findAny()
                                    .or(() -> playerQuests.getStartedOrActiveQuests()
                                                          .stream()
                                                          .filter(quest -> !quest.isMainQuest())
                                                          .findAny()
                                    )
                                    .ifPresentOrElse(
                                            quest -> PRNetworkHandler.sendToServer(new ServerFollowQuestPacket(quest.getId())),
                                            () -> PRNetworkHandler.sendToServer(new ServerFollowQuestPacket((ResourceLocation) null))
                                    );
                        break;
                    }
                    case LOGIN: {
                        renderer.setLevelProgressOverlay(GuiFactory.createLevelOverlay(clientData.getCurrentLevel().getDisplay(), clientData.getCurrentLevelProgress()));
                        if(clientData.getPlayerQuests().getFollowedQuest() != null) {
                            renderer.setQuestProgressOverlay(GuiFactory.createQuestOverlay(clientData.getPlayerQuests().getFollowedQuest().getDisplay(), clientData.getPlayerQuests().getFollowedQuestProgress()));
                        }

                        break;
                    }
                    case LOGOUT: {
                        renderer.setLevelProgressOverlay(null);
                        renderer.setQuestProgressOverlay(null);

                        break;
                    }
                }
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        this.displayInfo.serializeToNetwork(buf);
        buf.writeEnum(this.title);
    }


    public enum Type {
        LEVEL_COMPLETE,
        NEW_QUEST,
        QUEST_UPDATED,
        QUEST_COMPLETE,
        QUEST_FAILED,
        LOGIN,
        LOGOUT;


        public Component getTitle() {
            return switch(this) {
                case LEVEL_COMPLETE -> Component.literal("Level complete!");
                case NEW_QUEST -> Component.literal("New Quest!");
                case QUEST_UPDATED -> Component.literal("Quest updated!");
                case QUEST_COMPLETE -> Component.literal("Quest complete!");
                case QUEST_FAILED -> Component.literal("Quest failed!").withStyle(ChatFormatting.RED);
                default -> Component.empty();
            };
        }
    }
}
