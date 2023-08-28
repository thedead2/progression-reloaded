package de.thedead2.progression_reloaded.data.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;


public class FluidPredicate implements ITriggerPredicate<FluidState> {

    public static final ResourceLocation ID = ITriggerPredicate.createId("fluid");

    @SuppressWarnings("unchecked")
    public static final FluidPredicate ANY = new FluidPredicate(
            null,
            null,
            (StatePropertiesPredicate<FluidState>) StatePropertiesPredicate.ANY
    );

    @Nullable
    private final TagKey<Fluid> tag;

    @Nullable
    private final Fluid fluid;

    private final StatePropertiesPredicate<FluidState> properties;


    public FluidPredicate(@Nullable TagKey<Fluid> pTag, @Nullable Fluid pFluid, StatePropertiesPredicate<FluidState> pProperties) {
        this.tag = pTag;
        this.fluid = pFluid;
        this.properties = pProperties;
    }


    public static FluidPredicate from(FluidState fluidState) {
        if(fluidState == null) {
            return ANY;
        }
        return new FluidPredicate(null, fluidState.getType(), StatePropertiesPredicate.from(fluidState));
    }


    public static FluidPredicate fromJson(JsonElement pJson) {
        if(pJson != null && !pJson.isJsonNull()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "fluid");
            Fluid fluid = null;
            if(jsonobject.has("fluid")) {
                fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(GsonHelper.getAsString(jsonobject, "fluid")));
            }

            TagKey<Fluid> tagkey = null;
            if(jsonobject.has("tag")) {
                tagkey = TagKey.create(Registries.FLUID, new ResourceLocation(GsonHelper.getAsString(jsonobject, "tag")));
            }

            StatePropertiesPredicate<FluidState> statepropertiespredicate = StatePropertiesPredicate.fromJson(jsonobject.get("state"));

            return new FluidPredicate(tagkey, fluid, statepropertiespredicate);
        }
        else {
            return ANY;
        }
    }


    public boolean matches(FluidState fluidState, Object... addArgs) {
        if(this == ANY) {
            return true;
        }
        if(this.tag != null && !fluidState.is(this.tag)) {
            return false;
        }
        else if(this.fluid != null && !fluidState.is(this.fluid)) {
            return false;
        }
        else {
            return this.properties.matches(fluidState);
        }
    }


    public JsonElement toJson() {
        if(this == ANY) {
            return JsonNull.INSTANCE;
        }
        else {
            JsonObject jsonobject = new JsonObject();
            if(this.fluid != null) {
                jsonobject.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(this.fluid).toString());
            }

            if(this.tag != null) {
                jsonobject.addProperty("tag", this.tag.location().toString());
            }

            jsonobject.add("state", this.properties.toJson());
            return jsonobject;
        }
    }
}
