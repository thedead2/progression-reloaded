package de.thedead2.progression_reloaded.data.restrictions;

import de.thedead2.progression_reloaded.data.level.ProgressionLevel;
import de.thedead2.progression_reloaded.player.PlayerDataManager;
import de.thedead2.progression_reloaded.player.types.PlayerData;
import de.thedead2.progression_reloaded.util.ReflectionHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.registries.ModRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public abstract class Restriction<T> {

    @NotNull
    protected final ResourceLocation level;


    public Restriction(@NotNull ResourceLocation level) {
        this.level = level;
    }


    @SuppressWarnings("unchecked")
    public static <R> Restriction<R> deserializeFromNetwork(FriendlyByteBuf buf) {
        try {
            String className = buf.readUtf();
            ResourceLocation levelId = buf.readResourceLocation();
            Class<? extends Restriction<R>> restrictionClass = (Class<? extends Restriction<R>>) ReflectionHelper.findClassWithName(className);
            Method fromNet = restrictionClass.getDeclaredMethod("fromNetwork", FriendlyByteBuf.class, ResourceLocation.class);
            return (Restriction<R>) fromNet.invoke(null, buf, levelId);
        }
        catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            CrashHandler.getInstance().handleException("Failed to deserialize Restriction from network", e, Level.ERROR);
            throw new RuntimeException(e);
        }
    }


    public @NotNull ResourceLocation getLevel() {
        return level;
    }


    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.getClass().getName());
        buf.writeResourceLocation(this.level);
        this.toNetwork(buf);
    }


    protected abstract void toNetwork(FriendlyByteBuf buf);


    public boolean isActiveForPlayer(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        if(playerData != null) {
            ProgressionLevel level = playerData.getCurrentLevel();
            return !level.contains(ModRegistries.LEVELS.get().getValue(this.level));
        }
        return false;
    }
}
