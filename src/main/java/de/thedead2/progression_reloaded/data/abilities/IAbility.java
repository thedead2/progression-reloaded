package de.thedead2.progression_reloaded.data.abilities;

import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;

public interface IAbility {
    ResourceLocation getId();
    <T> boolean isPlayerAbleTo(T t);

    static ResourceLocation createId(String name){
        return new ResourceLocation(ModHelper.MOD_ID, "abilities_" + name);
    }
}
