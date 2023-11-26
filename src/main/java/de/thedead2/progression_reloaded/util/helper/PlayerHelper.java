package de.thedead2.progression_reloaded.util.helper;

import de.thedead2.progression_reloaded.client.ModClientInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;


public class PlayerHelper {

    public static Player getPlayerForUUID(UUID uuid) {
        Player player;
        if(FMLEnvironment.dist.isClient()) {
            player = ModClientInstance.getLocalPlayer();
            if(player != null && !player.getUUID().equals(uuid)) {
                throw new IllegalArgumentException("Can't get local player with uuid " + uuid + "! Current uuid of the client is: " + player.getUUID());
            }
        }
        else {
            player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        }
        return player;
    }
}
