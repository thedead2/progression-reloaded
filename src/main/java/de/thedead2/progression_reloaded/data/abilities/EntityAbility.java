package de.thedead2.progression_reloaded.data.abilities;

import com.google.gson.JsonElement;
import de.thedead2.progression_reloaded.data.predicates.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public class EntityAbility extends ListAbility<EntityPredicate> {
    public static final ResourceLocation ID = IAbility.createId("entity");
    protected EntityAbility(boolean blacklist, Collection<EntityPredicate> usable) {
        super(blacklist, usable);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }
}
