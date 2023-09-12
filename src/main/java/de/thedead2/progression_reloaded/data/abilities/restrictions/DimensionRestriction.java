package de.thedead2.progression_reloaded.data.abilities.restrictions;

import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class DimensionRestriction extends Restriction<ResourceKey<Level>> {

    @Nullable
    private final Component restrictionMessage;


    public DimensionRestriction(ResourceLocation levelId, RestrictionKey<ResourceKey<Level>> dimensionKey, @Nullable Component restrictionMessage) {
        super(levelId, dimensionKey);
        this.restrictionMessage = restrictionMessage;
    }


    protected static DimensionRestriction fromNetwork(FriendlyByteBuf buf, ResourceLocation levelId, RestrictionKey<ResourceKey<Level>> restrictionKey) {
        Component restrictionMessage = buf.readComponent();

        return new DimensionRestriction(levelId, restrictionKey, restrictionMessage);
    }


    @Nullable
    public Component getRestrictionMessage() {
        return restrictionMessage;
    }


    @Override
    protected void toNetwork(FriendlyByteBuf buf) {
        buf.writeNullable(this.restrictionMessage, FriendlyByteBuf::writeComponent);
    }
}
