package de.thedead2.progression_reloaded.data.abilities;

import de.thedead2.progression_reloaded.data.abilities.managers.DimensionRestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.managers.EntityRestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.managers.ItemRestrictionManager;
import net.minecraft.resources.ResourceLocation;

import static de.thedead2.progression_reloaded.data.AbilityManager.registerManager;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class ModRestrictionManagers {

    public static void register() {
        registerManager(new ResourceLocation(MOD_ID, "entity_restriction_manager"), new EntityRestrictionManager());
        registerManager(new ResourceLocation(MOD_ID, "dimension_restriction_manager"), new DimensionRestrictionManager());
        registerManager(new ResourceLocation(MOD_ID, "item_restriction_manager"), new ItemRestrictionManager());
    }

}
