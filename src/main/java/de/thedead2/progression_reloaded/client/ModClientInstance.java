package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import de.thedead2.progression_reloaded.data.RestrictionManager;
import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.events.PREventFactory;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;


/**
 * The singleton instance of the mod running on the logical client.
 * Use this instance to access the {@link ModRenderer}, {@link PlayerData} or {@link RestrictionManager}.
 */
//FIXME: Fix NumberFormatException: For input string: "xAlignment" --> failed to load options
@OnlyIn(Dist.CLIENT)
public class ModClientInstance {

    private static final ModClientInstance INSTANCE = new ModClientInstance();
    private final FontManager fontManager;

    private final Minecraft minecraft;

    private final ModRenderer modRenderer;

    private final RestrictionManager clientRestrictionManager;

    private PlayerData clientData;

    private boolean setOverlay = true;


    private ModClientInstance() {
        this.minecraft = Minecraft.getInstance();
        this.modRenderer = new ModRenderer();
        this.clientData = null;
        this.clientRestrictionManager = new RestrictionManager();
        this.fontManager = new FontManager();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this.modRenderer);
        modEventBus.addListener(this::onReloadListenerRegister);
    }

    private void onReloadListenerRegister(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(this.fontManager);
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


    public FontManager getFontManager() {
        return fontManager;
    }


    public PlayerData getClientData() {
        return clientData;
    }


    public void setClientData(PlayerData clientData) {
        ProgressionLevel previousLevel = this.clientData != null ? this.clientData.getCurrentLevel() : null;
        this.clientData = clientData;
        if(setOverlay) {
            this.modRenderer.setLevelProgressOverlay(GuiFactory.createLevelOverlay(this.clientData.getCurrentLevel().getDisplay(), this.clientData.getCurrentLevelProgress()));
            setOverlay = false;
        }

        this.modRenderer.updateLevelProgressOverlay(this.clientData.getCurrentLevelProgress());

        if(!this.clientData.getCurrentLevel().equals(previousLevel)) {
            PREventFactory.onLevelChanged(this.clientData.getCurrentLevel(), this.clientData, previousLevel);
        }

        PREventFactory.onPlayerSynced(this.clientData);
    }


    public RestrictionManager getClientRestrictionManager() {
        return clientRestrictionManager;
    }
}
