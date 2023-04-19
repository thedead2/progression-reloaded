package de.thedead2.progression_reloaded.progression_reloaded;

import de.thedead2.progression_reloaded.ItemProgression;
import de.thedead2.progression_reloaded.PCommonProxy;
import de.thedead2.progression_reloaded.commands.ModCommand;
import de.thedead2.progression_reloaded.crafting.ActionType;
import de.thedead2.progression_reloaded.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.commands.CommandManager;
import de.thedead2.progression_reloaded.handlers.RemappingHandler;
import de.thedead2.progression_reloaded.helpers.FileHelper;
import de.thedead2.progression_reloaded.helpers.ModLogHelper;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.json.Options;
import de.thedead2.progression_reloaded.player.PlayerSavedData;
import de.thedead2.progression_reloaded.plugins.enchiridion.EnchiridionSupport;
import de.thedead2.progression_reloaded.plugins.thaumcraft.ThaumcraftSupport;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static de.thedead2.progression_reloaded.progression_reloaded.util.ModHelper.MOD_ID;
import static de.thedead2.progression_reloaded.lib.PInfo.*;

@Mod(MOD_ID)
public class ProgressionReloaded {

    ProgressionReloaded(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext loadingContext = ModLoadingContext.get();
        loadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.CONFIG_SPEC, MOD_ID + "-common.toml");

        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {//PreInit Event?
        //Init the action types
        ActionType.init();
        

        /*proxy.preInit(event.getAsmData());
        proxy.initClient();*/
    }

    /*@SidedProxy(clientSide = JAVAPATH + "PClientProxy", serverSide = JAVAPATH + "PCommonProxy")
    public static PCommonProxy proxy;*/

    /*@Instance(MODID)
    public static de.thedead2.progression_reloaded.ProgressionReloaded instance; //what is that for?*/

    public static PlayerSavedData data = new PlayerSavedData(MODNAME);
    public static ItemProgression item;


    private void init(FMLInitializationEvent event) {
        //proxy.registerRendering();
    }

    private void onServerStarting(final ServerStartingEvent event){
        World world = event.getServer().worldServers[0];
        data = (PlayerSavedData) world.loadItemData(PlayerSavedData.class, MODNAME);
        if (data == null) {
            createWorldData(world);
        }

        //Remap all relevant data, Loads in the server data
        RemappingHandler.reloadServerData(JSONLoader.getServerTabData(RemappingHandler.getHostName()), false);
    }


    private void onServerStarting(final RegisterCommandsEvent event) {
        ModCommand.registerCommands(event.getDispatcher());
    }

    public void createWorldData(World world) {
        data = new PlayerSavedData(MODNAME);
        world.setItemData(MODNAME, data);
    }

    public static String translate(String string) {
        return I18n.get("progression." + string);
    } //replace with TranslationKeyProvider

    public static String format(String string, Object... object) {
        return StringEscapeUtils.unescapeJava(I18n.get("progression." + string, object));
    } //uses?
}
