package de.thedead2.progression_reloaded.client;

import de.thedead2.progression_reloaded.client.data.ClientPlayer;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientManager {

    private static final ClientManager instance = new ClientManager();

    private ClientPlayer player;


    @SubscribeEvent
    public static void onClientPlayerLogIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        instance.player = new ClientPlayer();
    }


    @SubscribeEvent
    public static void onClientPlayerLogOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        instance.player = null;
    }


    public static ClientManager getInstance() {
        return instance;
    }


    @Nullable
    public ClientPlayer getPlayer() {
        return player;
    }
}
