package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.data.LevelManager;
import de.thedead2.progression_reloaded.items.custom.ExtraLifeItem;
import de.thedead2.progression_reloaded.network.PRNetworkHandler;
import de.thedead2.progression_reloaded.network.packets.ClientOnProgressChangedPacket;
import de.thedead2.progression_reloaded.network.packets.ClientSyncPlayerDataPacket;
import de.thedead2.progression_reloaded.network.packets.ClientUsedExtraLifePacket;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ConfigManager;
import de.thedead2.progression_reloaded.util.GameState;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static de.thedead2.progression_reloaded.util.ModHelper.GAME_STATE;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;
import static de.thedead2.progression_reloaded.util.helper.MathHelper.secondsToTicks;


@Mod.EventBusSubscriber(modid = ModHelper.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerListeners {

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        GAME_STATE = GameState.PLAYER_LOGGED_IN;
        Player player = event.getEntity();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        LevelManager.getInstance().checkForCreativeMode(playerData);
        LevelManager.getInstance().updateStatus();
    }


    //We're only hooking in when the player is being placed in the world, the connection is established, and the UpdateRecipesEvent hasn't fired yet
    @SubscribeEvent
    public static void onDataPackReload(final OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if(player != null) {
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(playerData), player);
            LevelManager.getInstance().getRestrictionManager().syncRestrictions(player);
            PRNetworkHandler.sendToPlayer(new ClientOnProgressChangedPacket(playerData.getCurrentLevel().getDisplay(), ClientOnProgressChangedPacket.Type.LOGIN), player);
        }
    }


    @SubscribeEvent
    public static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        GAME_STATE = GameState.PLAYER_LOGGED_OUT;
        PlayerData data = PlayerDataManager.getPlayerData(event.getEntity());
        PRNetworkHandler.sendToPlayer(new ClientOnProgressChangedPacket(data.getCurrentLevel().getDisplay(), ClientOnProgressChangedPacket.Type.LOGOUT), data.getServerPlayer());
    }


    @SubscribeEvent
    public static void onPlayerFileLoad(final PlayerEvent.LoadFromFile event) {
        PlayerDataManager.loadPlayerData(event.getPlayerFile(MOD_ID), (ServerPlayer) event.getEntity());
    }


    @SubscribeEvent
    public static void onPlayerFileSave(final PlayerEvent.SaveToFile event) {
        PlayerDataManager.savePlayerData(event.getEntity(), event.getPlayerFile(MOD_ID));
        PlayerData data = PlayerDataManager.getPlayerData(event.getEntity());
        PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(data), data.getServerPlayer());

        if(GAME_STATE == GameState.PLAYER_LOGGED_OUT) {
            Player player = event.getEntity();
            PlayerDataManager.clearPlayerData(player);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(final LivingDeathEvent event) {
        if(event.getEntity() instanceof ServerPlayer serverPlayer && !serverPlayer.level.isClientSide()) {
            PlayerData player = PlayerDataManager.getPlayerData(serverPlayer);
            if(player.hasAndUpdateExtraLives() || ExtraLifeItem.isUnlimited()) {
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
                serverPlayer.removeAllEffects();
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, secondsToTicks(30), 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, secondsToTicks(15), 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.SATURATION, secondsToTicks(25), 1));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, secondsToTicks(15), 0));
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.CONFUSION, secondsToTicks(7)));
                event.setCanceled(true);
                PRNetworkHandler.sendToPlayer(new ClientUsedExtraLifePacket(serverPlayer), serverPlayer);
                PRNetworkHandler.sendToPlayer(new ClientSyncPlayerDataPacket(player), serverPlayer);
            }
        }
    }


    @SubscribeEvent
    public static void onGameModeChange(final PlayerEvent.PlayerChangeGameModeEvent event) {
        if(!ConfigManager.CHANGE_LEVEL_ON_CREATIVE.get()) {
            return;
        }
        GameType newGameMode = event.getNewGameMode();
        GameType currentGameMode = event.getCurrentGameMode();
        if(isGameModeCreativeOrSpectator(newGameMode) && isGameModeNotCreativeAndSpectator(currentGameMode)) {
            LevelManager.getInstance().onCreativeChange(event.getEntity());
        }
        else if(isGameModeNotCreativeAndSpectator(newGameMode) && isGameModeCreativeOrSpectator(currentGameMode)) {
            LevelManager.getInstance().onSurvivalChange(event.getEntity());
        }
    }


    private static boolean isGameModeCreativeOrSpectator(GameType gameMode) {
        return gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR;
    }


    private static boolean isGameModeNotCreativeAndSpectator(GameType gameMode) {
        return gameMode != GameType.CREATIVE && gameMode != GameType.SPECTATOR;
    }

}
