package de.thedead2.progression_reloaded.util;

import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import net.minecraftforge.common.ForgeConfigSpec;

public abstract class ConfigManager {
    private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    /**
     * All Config fields for Progression Reloaded
     **/
    public static final ForgeConfigSpec.BooleanValue OUT_DATED_MESSAGE;
    public static final ForgeConfigSpec.BooleanValue DISABLE_ADVANCEMENTS;


    static {
        CONFIG_BUILDER.push("Config for " + ModHelper.MOD_NAME);

        OUT_DATED_MESSAGE = newBoolean("Whether the mod should send a chat message if an update is available","warnMessage", true);

        DISABLE_ADVANCEMENTS = newBoolean("Whether the mod should disable minecrafts in-game advancements", "disableAdvancements", true);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

    public static ForgeConfigSpec.BooleanValue newBoolean(String comment, String name, boolean defaultValue){
        return CONFIG_BUILDER.comment(comment + ".\ndefault = " + defaultValue).define(name, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number, V extends ForgeConfigSpec.ConfigValue<T>> V newRange(String comment, String name, T defaultValue, T min, T max){
        CONFIG_BUILDER.comment(comment);
        if(defaultValue instanceof Integer) return (V) CONFIG_BUILDER.defineInRange(name, (int) defaultValue, (int) min,(int) max);
        else if (defaultValue instanceof Double) return (V) CONFIG_BUILDER.defineInRange(name, (double) defaultValue, (double) min, (double) max);
        else if(defaultValue instanceof Long) return (V) CONFIG_BUILDER.defineInRange(name, (long) defaultValue, (long) min, (long) max);
        else throw new IllegalArgumentException("Unsupported number type: " + defaultValue.getClass());
    }
}