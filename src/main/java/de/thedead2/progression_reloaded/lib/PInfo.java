package de.thedead2.progression_reloaded.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//Most of this is in ModHelper Class, so use it there
public class PInfo {
    public static final String MODID = "progression";
    public static final String MODNAME = "Progression";
    public static final String MODPATH = "progression";

    public static final String VERSION = "@VERSION@";
    public static final String GUI_FACTORY_CLASS = "config.gui.de.the_dead_2.progression_reloaded.GuiFactory";
    public static final String BOOKPATH = "progression:textures/books/";
    public static final String FILTER = "criteria.api.de.the_dead_2.progression_reloaded.IFilterProvider";
    
    @SideOnly(Side.CLIENT)
    public static final ResourceLocation textures = new ResourceLocation(MODPATH, "textures/gui/textures.png");
}
