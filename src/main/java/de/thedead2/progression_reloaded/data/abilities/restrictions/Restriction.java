package de.thedead2.progression_reloaded.data.abilities.restrictions;

import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import de.thedead2.progression_reloaded.util.ReflectionHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public abstract class Restriction<T> {

    @NotNull
    protected final ResourceLocation levelId;

    protected final RestrictionKey<T> key;


    public Restriction(@NotNull ResourceLocation levelId, RestrictionKey<T> key) {
        this.levelId = levelId;
        this.key = key;
    }


    @SuppressWarnings("unchecked")
    public static <R> Restriction<R> deserializeFromNetwork(FriendlyByteBuf buf) {
        try {
            String className = buf.readUtf();
            ResourceLocation levelId = buf.readResourceLocation();
            RestrictionKey<R> restrictionKey = RestrictionKey.fromNetwork(buf);
            Class<? extends Restriction<R>> restrictionClass = (Class<? extends Restriction<R>>) ReflectionHelper.findClassWithName(className);
            Method fromNet = restrictionClass.getDeclaredMethod("fromNetwork", FriendlyByteBuf.class, ResourceLocation.class, RestrictionKey.class);
            return (Restriction<R>) fromNet.invoke(null, buf, levelId, restrictionKey);
        }
        catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            CrashHandler.getInstance().handleException("Failed to deserialize Restriction from network", e, Level.ERROR);
            throw new RuntimeException(e);
        }
    }


    public ResourceLocation getLevel() {
        return levelId;
    }


    public RestrictionKey<T> getKey() {
        return key;
    }


    public void serializeToNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.getClass().getName());
        buf.writeResourceLocation(this.levelId);
        this.key.toNetwork(buf);
        this.toNetwork(buf);
    }


    protected abstract void toNetwork(FriendlyByteBuf buf);
}
