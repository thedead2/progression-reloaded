package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.data.abilities.IAbility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AbilityManager { //TODO: implement abilities
    public static <T> void handleAbilityRequest(Class<? extends IAbility<T>> abilityClass, Entity entity, T t, Action<T> abilityAction) {
        if(entity instanceof Player player){
            /*SinglePlayer singlePlayer = PlayerDataHandler.getActivePlayer(player);
            if(!singlePlayer.getPlayer().isCreative()) {
                if(singlePlayer.getAbility(abilityClass).isPlayerAbleTo(t)){
                    abilityAction.run(t);
                }
            }*/
        }
    }

    @FunctionalInterface
    public interface Action<T>{
        void run(T t);
    }
}
