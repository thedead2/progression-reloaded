package de.thedead2.progression_reloaded.api.special;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Implement this on objects that have a miniature icon **/
public interface IMiniIcon {
    @OnlyIn(Dist.CLIENT)
    public ItemStack getMiniIcon();
}
