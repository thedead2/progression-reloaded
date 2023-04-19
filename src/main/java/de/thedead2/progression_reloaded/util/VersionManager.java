package de.thedead2.progression_reloaded.progression_reloaded.util;

import de.thedead2.progression_reloaded.progression_reloaded.util.language.TranslationKeyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.VersionChecker;

/**
 * Inner Class VersionManager
 * handles every Update related action
 **/
public abstract class VersionManager {

    private static final VersionChecker.CheckResult RESULT = VersionChecker.getResult(ModHelper.THIS_MOD_CONTAINER.getModInfo());


    public static void sendChatMessage(Player player) {
        if (RESULT.status().equals(VersionChecker.Status.OUTDATED)) {
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("mod_outdated_message", ChatFormatting.RED));
            player.sendSystemMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, ChatFormatting.RED));
        } else if (RESULT.status().equals(VersionChecker.Status.BETA)) {
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_warn_message", ChatFormatting.YELLOW));
        } else if (RESULT.status().equals(VersionChecker.Status.BETA_OUTDATED)) {
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_warn_message", ChatFormatting.YELLOW));
            player.sendSystemMessage(TranslationKeyProvider.chatMessage("beta_outdated_message", ChatFormatting.RED));
            player.sendSystemMessage(TranslationKeyProvider.chatLink(ModHelper.MOD_UPDATE_LINK, ChatFormatting.RED));}
    }

    public static void sendLoggerMessage() {
        if (RESULT.status().equals(VersionChecker.Status.OUTDATED)) {
            ModHelper.LOGGER.warn("Mod is outdated! Current Version: " + ModHelper.MOD_VERSION + " Latest Version: " + RESULT.target());
            ModHelper.LOGGER.warn("Please update " + ModHelper.MOD_NAME + " using this link: " + ModHelper.MOD_UPDATE_LINK);
        } else if (RESULT.status().equals(VersionChecker.Status.FAILED)) {
            ModHelper.LOGGER.error("Failed to check for updates! Please check your internet connection!");
        } else if (RESULT.status().equals(VersionChecker.Status.BETA)) {
            ModHelper.LOGGER.warn("You're currently using a Beta of " + ModHelper.MOD_NAME + "! Please note that using this beta is at your own risk!");
            ModHelper.LOGGER.info("Beta Status: " + RESULT.status());
        } else if (RESULT.status().equals(VersionChecker.Status.BETA_OUTDATED)) {
            ModHelper.LOGGER.warn("You're currently using a Beta of " + ModHelper.MOD_NAME + "! Please note that using this beta is at your own risk!");
            ModHelper.LOGGER.warn("This Beta is outdated! Please update " + ModHelper.MOD_NAME + " using this link: " + ModHelper.MOD_UPDATE_LINK);
            ModHelper.LOGGER.warn("Beta Status: " + RESULT.status());
        }
    }
}
