package de.thedead2.progression_reloaded.handlers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import de.thedead2.progression_reloaded.crafting.CraftingRegistry;
import de.thedead2.progression_reloaded.json.DefaultSettings;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.network.PacketHandler;
import de.thedead2.progression_reloaded.network.PacketSyncJSONToClient;
import de.thedead2.progression_reloaded.network.PacketSyncJSONToClient.Section;
import de.thedead2.progression_reloaded.player.PlayerTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collection;
import java.util.List;

public class RemappingHandler {
    public static Multimap<ICriteria, ICriteria> criteriaToUnlocks; //A list of the critera completing this one unlocks
    
    public static String getHostName() {
        String hostname = FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()? FMLCommonHandler.instance().getMinecraftServerInstance().getServerHostname(): "ssp";  
        if (hostname.equals("")) hostname = "smp";
        return hostname;
    }

    //Grabs the json, as a split string and rebuilds it, sends it in parts to the client
    public static void onPlayerConnect(EntityPlayerMP player) {
        //Remap the player data, for this player, before doing anything else, as the data may not existing yet
        PlayerTracker.getServerPlayer(player).getTeam().rebuildTeamCache(); //Rebuild the team cache
        PlayerTracker.getServerPlayer(player).getMappings().remap();
        PacketHandler.sendToClient(new PacketSyncJSONToClient(Section.SEND_HASH, JSONLoader.serverHashcode, getHostName()), player);
    }

    /** Called, When a server starts, or when reload or reset is called **/
    public static void reloadServerData(DefaultSettings settings, boolean isClient) {
        //Reset the data
        //Create a a new unlocker
        criteriaToUnlocks = HashMultimap.create(); //Reset all data
        //All data has officially been wiped SERVERSIDE
        //Reload in all the data from json
        /** Grab yourself some gson, load it in from the file serverside **/
        JSONLoader.loadJSON(false, settings); //This fills out all the data once again

        //Now that mappings have been synced to the client reload the unlocks list
        Collection<ICriteria> allCriteria = APICache.getCache(isClient).getCriteriaSet();
        for (ICriteria criteria : allCriteria) { //Remap criteria to unlocks
            //We do not give a damn about whether this is available or not
            //The unlocking of criteria should happen no matter what
            List<ICriteria> requirements = criteria.getPreReqs();
            for (ICriteria require : requirements) {
                criteriaToUnlocks.get(require).add(criteria);
            }
        }
    }

    public static void resetRegistries(boolean isClientside) {
        //Resets all of the registries to default empty data
        APICache.resetAPIHandler(isClientside); //Reset tabs and criteria maps
        EventsManager.resetEvents(isClientside);
        CraftingRegistry.resetRegistry(isClientside);
    }
}
