package de.thedead2.progression_reloaded.data.abilities.restrictions;

import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;


public class RecipeRestriction extends Restriction<Recipe<?>> {


    public RecipeRestriction(@NotNull ResourceLocation levelId, RestrictionKey<Recipe<?>> key) {
        super(levelId, key);
    }


    @Override
    protected void toNetwork(FriendlyByteBuf buf) {

    }
}
