package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.client.gui.fonts.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


/**
 * The singleton instance of the mod running on the logical client.
 * Use this instance to access the {@link ModRenderer}, {@link ClientDataHolder} or {@link ClientRestrictionManager}.
 */
@OnlyIn(Dist.CLIENT)
public class ModClientInstance {

    private static final ModClientInstance INSTANCE = new ModClientInstance();
    private final FontManager fontManager;

    private final Minecraft minecraft;

    private final ModRenderer modRenderer;

    private final ClientDataHolder clientDataHolder;

    private final ClientRestrictionManager clientRestrictionManager;


    private ModClientInstance() {
        this.minecraft = Minecraft.getInstance();
        this.modRenderer = new ModRenderer();
        this.clientDataHolder = new ClientDataHolder();
        this.clientRestrictionManager = new ClientRestrictionManager();
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


    public ClientDataHolder getClientDataManager() {
        return clientDataHolder;
    }


    public ClientRestrictionManager getClientRestrictionManager() {
        return clientRestrictionManager;
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
}
