package de.thedead2.progression_reloaded.data.restrictions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;


public class EntityRestriction extends Restriction<EntityType<?>> {

    @Nullable
    private final ResourceLocation dimensionId;

    private final boolean allowSpawns;

    private final int distanceToPlayer;

    private final boolean allowSpawners;

    @Nullable
    private final ResourceLocation entityReplacement;


    public EntityRestriction(
            ResourceLocation levelId, @Nullable ResourceLocation dimensionId, boolean allowSpawns, int distanceToPlayer, boolean allowSpawners,
            @Nullable ResourceLocation entityReplacement
    ) {
        super(levelId);
        this.dimensionId = dimensionId;
        this.allowSpawns = allowSpawns;
        this.distanceToPlayer = distanceToPlayer;
        this.allowSpawners = allowSpawners;
        this.entityReplacement = entityReplacement;
    }


    protected static EntityRestriction fromNetwork(FriendlyByteBuf buf, ResourceLocation levelId) {
        ResourceLocation dimensionId = buf.readNullable(FriendlyByteBuf::readResourceLocation);
        boolean allowSpawns = buf.readBoolean();
        int distanceToPlayer = buf.readInt();
        boolean allowSpawners = buf.readBoolean();
        ResourceLocation entityReplacement = buf.readNullable(FriendlyByteBuf::readResourceLocation);

        return new EntityRestriction(levelId, dimensionId, allowSpawns, distanceToPlayer, allowSpawners, entityReplacement);
    }


    @Nullable
    public ResourceLocation getDimension() {
        return dimensionId;
    }


    @Nullable
    public ResourceLocation getEntityReplacement() {
        return entityReplacement;
    }


    public int getDistanceToPlayer() {
        return distanceToPlayer;
    }


    public boolean allowSpawners() {
        return allowSpawners;
    }


    public boolean allowSpawns() {
        return allowSpawns;
    }


    @Override
    protected void toNetwork(FriendlyByteBuf buf) {
        buf.writeNullable(this.dimensionId, FriendlyByteBuf::writeResourceLocation);
        buf.writeBoolean(this.allowSpawns);
        buf.writeInt(this.distanceToPlayer);
        buf.writeBoolean(this.allowSpawners);
        buf.writeNullable(this.entityReplacement, FriendlyByteBuf::writeResourceLocation);
    }
}
