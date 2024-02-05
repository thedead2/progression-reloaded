package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.data.RestrictionManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.data.quest.ProgressionQuest;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;


/**
 * The singleton instance of the mod running on the logical client.
 * Use this instance to access the {@link ModRenderer}, {@link PlayerData} or {@link RestrictionManager}.
 */
//FIXME: Fix NumberFormatException: For input string: "xAlignment" --> failed to load options
@OnlyIn(Dist.CLIENT)
public class ModClientInstance {

    private static final ModClientInstance INSTANCE = new ModClientInstance();

    private final Minecraft minecraft;

    private final ModRenderer modRenderer;

    private final RestrictionManager clientRestrictionManager;

    private PlayerData clientData;


    private ModClientInstance() {
        this.minecraft = Minecraft.getInstance();
        this.modRenderer = new ModRenderer();
        this.clientData = null;
        this.clientRestrictionManager = new RestrictionManager();
    }


    public static ModClientInstance getInstance() {
        return INSTANCE;
    }


    public static boolean isUndo(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }


    public static boolean isRedo(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_Z && Screen.hasControlDown() && Screen.hasShiftDown() && !Screen.hasAltDown();
    }


    public static LocalPlayer getLocalPlayer() {
        return Minecraft.getInstance().player;
    }


    public Minecraft getMinecraft() {
        return minecraft;
    }


    public ModRenderer getModRenderer() {
        return modRenderer;
    }


    public PlayerData getClientData() {
        return clientData;
    }


    public void setClientData(PlayerData clientData) {
        ProgressionLevel previousLevel = this.clientData != null ? this.clientData.getCurrentLevel() : null;
        ProgressionQuest previousFollowedQuest = this.clientData != null ? this.clientData.getPlayerQuests().getFollowedQuest() : null;
        this.clientData = clientData;

        if(!this.clientData.getCurrentLevel().equals(previousLevel)) {
            PREventFactory.onLevelChanged(this.clientData.getCurrentLevel(), this.clientData, previousLevel);
        }

        if(this.clientData.getPlayerQuests().getFollowedQuest() != null && !this.clientData.getPlayerQuests().getFollowedQuest().equals(previousFollowedQuest)) {
            this.modRenderer.updateQuestProgressOverlay(this.clientData.getPlayerQuests().getFollowedQuestProgress());
            PREventFactory.onQuestFocusChanged(this.clientData.getPlayerQuests().getFollowedQuest(), this.clientData, previousFollowedQuest);
        }

        PREventFactory.onPlayerSynced(this.clientData);
    }


    public RestrictionManager getClientRestrictionManager() {
        return clientRestrictionManager;
    }
}
