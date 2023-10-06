package de.thedead2.progression_reloaded.data.abilities;

import de.thedead2.progression_reloaded.data.abilities.managers.BlockRestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.managers.DimensionRestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.managers.EntityRestrictionManager;
import de.thedead2.progression_reloaded.data.abilities.managers.ItemRestrictionManager;
import de.thedead2.progression_reloaded.util.helper.ResourceLocationHelper;
import net.minecraft.resources.ResourceLocation;

import static de.thedead2.progression_reloaded.data.AbilityManager.registerManager;
import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class ModRestrictionManagers {

    public static final EntityRestrictionManager ENTITY_RESTRICTION_MANAGER = registerManager(new ResourceLocation(MOD_ID, "entity_restriction_manager"), new EntityRestrictionManager());

    public static final DimensionRestrictionManager DIMENSION_RESTRICTION_MANAGER = registerManager(new ResourceLocation(MOD_ID, "dimension_restriction_manager"), new DimensionRestrictionManager());

    public static final ItemRestrictionManager ITEM_RESTRICTION_MANAGER = registerManager(new ResourceLocation(MOD_ID, "item_restriction_manager"), new ItemRestrictionManager());

    public static final BlockRestrictionManager BLOCK_RESTRICTION_MANAGER = registerManager(ResourceLocationHelper.createId("block_restriction_manager"), new BlockRestrictionManager());
    public static void register() {

    }

}
