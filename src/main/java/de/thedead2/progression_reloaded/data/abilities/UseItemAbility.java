package de.thedead2.progression_reloaded.data.abilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UseItemAbility implements IAbility{
    private final Set<ItemStack> usableItems = new HashSet<>();
    private final boolean blacklist;

    public UseItemAbility(boolean blacklist, Collection<ItemStack> items){
        this.blacklist = blacklist;
        usableItems.addAll(items);
    }
    @Override
    public ResourceLocation getId() {
        return IAbility.createId("item");
    }

    @Override
    public <T> boolean isPlayerAbleTo(T t) {
        if(t instanceof ItemStack itemStack){
            return blacklist == usableItems.contains(itemStack);
        }
        return false;
    }
}
