package de.thedead2.progression_reloaded.progression_reloaded.util;

import net.minecraftforge.common.ForgeConfigSpec;

public abstract class ConfigManager {

    private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;

    /**
     * All Config fields for Progression Reloaded
     **/

    public static final ForgeConfigSpec.ConfigValue<Boolean> TILE_CLAIMER_RECIPE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> OVERWRITE_CRITERIA_JSON_FOR_CLIENTS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_CRITERIA_BACKUPS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAXIMUM_CRITERIA_BACKUPS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MUST_CLAIM_DEFAULT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HARD_RESET;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HIDE_TOOLTIPS;


    static {
        CONFIG_BUILDER.push("Config for " + ModHelper.MOD_NAME);

        TILE_CLAIMER_RECIPE = CONFIG_BUILDER.comment("Add Recipe for Tile Entity Claimer").define("tileClaimerRecipe", true);

        OVERWRITE_CRITERIA_JSON_FOR_CLIENTS = CONFIG_BUILDER.comment("Overwrite criteria.json", "If this is true then Clients will always use the criteria.json file, and have it overridden by whatever is on a server,",
                                            "by default this is false, which means clients will create a new json file for every server they join, so that the data,",
                                            "is cached instead of being recreated everytime they join a new server. This setting being false means that if you are editing",
                                            "criteria on a server, for editing a pack, then you need to give users the serverside criteria.json and not the one in your client folder").define("overwriteCriteriaJsonForClients", false);

        ENABLE_CRITERIA_BACKUPS = CONFIG_BUILDER.comment("Enable Criteria Backups", "Criteria will be backed up, whenever it's saved if this is true").define("enableCriteriaBackups", true);

        MAXIMUM_CRITERIA_BACKUPS = CONFIG_BUILDER.comment("Maximum Criteria Backups").defineInRange("maxCriteriaBackups", 25, 1, 100);

        MUST_CLAIM_DEFAULT = CONFIG_BUILDER.comment("Default Setting for Claiming", "If this is true, new rewards will be set to mustClaim = true by default").define("mustClaimDefault", false);

        HARD_RESET = CONFIG_BUILDER.comment("Remove Players from Teams when Resetting Data", "When this is true, players will be removed from their teams when you execute the progression reset command").define("hardReset", false);

        HIDE_TOOLTIPS = CONFIG_BUILDER.comment("Hide Editor Tooltips", "With this set to true the information tooltips when editing will be removed").define("hideTooltips", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }
}