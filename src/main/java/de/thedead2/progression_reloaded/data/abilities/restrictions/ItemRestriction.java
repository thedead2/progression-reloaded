package de.thedead2.progression_reloaded.data.abilities.restrictions;

import de.thedead2.progression_reloaded.data.abilities.RestrictionKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


public class ItemRestriction extends Restriction<Item> {

    private final boolean allowedInInventory;

    private final boolean allowedEquipped;

    private final boolean allowedAttacking;

    private final boolean allowedPickup;

    private final boolean allowedUsing;

    private final boolean hideInJEI;


    public ItemRestriction(ResourceLocation levelId, RestrictionKey<Item> itemId) {
        this(levelId, itemId, false, false, false, false, false, true);
    }


    public ItemRestriction(
            ResourceLocation levelId, RestrictionKey<Item> itemId, boolean allowedInInventory, boolean allowedEquipped, boolean allowedAttacking, boolean allowedPickup, boolean allowedUsing,
            boolean hideInJEI
    ) {
        super(levelId, itemId);
        this.allowedInInventory = allowedInInventory;
        this.allowedEquipped = allowedEquipped;
        this.allowedAttacking = allowedAttacking;
        this.allowedPickup = allowedPickup;
        this.allowedUsing = allowedUsing;
        this.hideInJEI = hideInJEI;
    }


    protected static ItemRestriction fromNetwork(FriendlyByteBuf buf, ResourceLocation levelId, RestrictionKey<Item> restrictionKey) {
        boolean allowedInInventory = buf.readBoolean();
        boolean allowedEquipped = buf.readBoolean();
        boolean allowedAttacking = buf.readBoolean();
        boolean allowedPickup = buf.readBoolean();
        boolean allowedUsing = buf.readBoolean();
        boolean hideInJEI = buf.readBoolean();

        return new ItemRestriction(levelId, restrictionKey, allowedInInventory, allowedEquipped, allowedAttacking, allowedPickup, allowedUsing, hideInJEI);
    }


    @Override
    protected void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(this.allowedInInventory);
        buf.writeBoolean(this.allowedEquipped);
        buf.writeBoolean(this.allowedAttacking);
        buf.writeBoolean(this.allowedPickup);
        buf.writeBoolean(this.allowedUsing);
        buf.writeBoolean(this.hideInJEI);
    }


    public boolean isAllowedForAttacking() {
        return allowedAttacking;
    }


    public boolean isAllowedEquipped() {
        return allowedEquipped;
    }


    public boolean isAllowedInInventory() {
        return allowedInInventory;
    }


    public boolean isAllowedToBePickedUp() {
        return allowedPickup;
    }


    public boolean isAllowedToBeUsed() {
        return allowedUsing;
    }


    public boolean shouldHideInJEI() {
        return hideInJEI;
    }
}
