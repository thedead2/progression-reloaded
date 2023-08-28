package de.thedead2.progression_reloaded.util;

import de.thedead2.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;


public abstract class VersionManager {

    private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(ModHelper.THIS_MOD_CONTAINER().getModInfo());


    public static void register(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(VersionManager::onLoadComplete);
        forgeEventBus.addListener(VersionManager::onPlayerLogin);
    }


    public static void onLoadComplete(final FMLLoadCompleteEvent event) {
        VersionManager.sendLoggerMessage();
    }


    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if(ConfigManager.OUT_DATED_MESSAGE.get() /*&& !isDevEnv()*/) {
            VersionManager.sendChatMessage(event.getEntity());
        }
    }


    public static void sendLoggerMessage() {
        if(RESULT.status().equals(VersionChecker.Status.OUTDATED)) {
            ModHelper.LOGGER.warn("Mod is outdated! Current Version: " + ModHelper.MOD_VERSION + " Latest Version: " + RESULT.target());
            ModHelper.LOGGER.warn("Please update " + ModHelper.MOD_NAME + " using this link: " + ModHelper.MOD_UPDATE_LINK);
        }
        else if(RESULT.status().equals(VersionChecker.Status.FAILED)) {
            ModHelper.LOGGER.error("Failed to check for updates! Please check your internet connection!");
        }
        else if(RESULT.status().equals(VersionChecker.Status.BETA)) {
            ModHelper.LOGGER.warn("You're currently using a Beta of " + ModHelper.MOD_NAME + "! Please note that using this alpha/beta is at your own risk!");
            ModHelper.LOGGER.info("Alpha/Beta Status: " + RESULT.status());
        }
        else if(RESULT.status().equals(VersionChecker.Status.BETA_OUTDATED)) {
            ModHelper.LOGGER.warn("You're currently using a alpha/beta of " + ModHelper.MOD_NAME + "! Please note that using this alpha/beta is at your own risk!");
            ModHelper.LOGGER.warn("This Alpha/Beta is outdated! Please update " + ModHelper.MOD_NAME + " using this link: " + ModHelper.MOD_UPDATE_LINK);
            ModHelper.LOGGER.warn("Beta Status: " + RESULT.status());
        }
    }


    public static void sendChatMessage(Player player) {
        if(RESULT.status().equals(VersionChecker.Status.OUTDATED)) {
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("mod_outdated_message", ChatFormatting.RED));
            player.sendSystemMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, ChatFormatting.RED));
        }
        else if(RESULT.status().equals(VersionChecker.Status.BETA)) {
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_warn_message", ChatFormatting.YELLOW));
        }
        else if(RESULT.status().equals(VersionChecker.Status.BETA_OUTDATED)) {
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_warn_message", ChatFormatting.YELLOW));
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_outdated_message", ChatFormatting.RED));
            player.sendSystemMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, ChatFormatting.RED));
        }
    }
}
