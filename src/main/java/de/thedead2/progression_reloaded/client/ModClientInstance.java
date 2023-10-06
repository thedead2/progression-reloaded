package de.thedead2.progression_reloaded.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;


/**
 * The singleton instance of the mod running on the logical client.
 * Use this instance to access the {@link ModRenderer}, {@link ClientDataManager} or {@link ClientRestrictionManager}.
 */
@OnlyIn(Dist.CLIENT)
public class ModClientInstance {

    private static final ModClientInstance INSTANCE = new ModClientInstance();

    private final Minecraft minecraft;

    private final ModRenderer modRenderer;

    private final ClientDataManager clientDataManager;

    private final ClientRestrictionManager clientRestrictionManager;


    private ModClientInstance() {
        this.minecraft = Minecraft.getInstance();
        this.modRenderer = new ModRenderer();
        this.clientDataManager = new ClientDataManager();
        this.clientRestrictionManager = new ClientRestrictionManager();

        MinecraftForge.EVENT_BUS.register(this.modRenderer);
    }


    public static ModClientInstance getInstance() {
        return INSTANCE;
    }


    public ClientDataManager getClientDataManager() {
        return clientDataManager;
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
}
