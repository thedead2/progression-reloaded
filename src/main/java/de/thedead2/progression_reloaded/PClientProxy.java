package de.thedead2.progression_reloaded;

import de.thedead2.progression_reloaded.ItemProgressionRenderer.ProgressionOverride;
import de.thedead2.progression_reloaded.handlers.RemappingHandler;
import de.thedead2.progression_reloaded.handlers.TemplateHandler;
import de.thedead2.progression_reloaded.helpers.ChatHelper;
import de.thedead2.progression_reloaded.helpers.RenderItemHelper;
import de.thedead2.progression_reloaded.json.Options;
import de.thedead2.progression_reloaded.lib.GuiIDs;
import de.thedead2.progression_reloaded.lib.PInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import static de.thedead2.progression_reloaded.ProgressionReloaded.translate;
import static de.thedead2.progression_reloaded.gui.core.GuiList.CORE;
import static de.thedead2.progression_reloaded.gui.core.GuiList.GROUP_EDITOR;
import static net.minecraft.util.text.TextFormatting.GOLD;

public class PClientProxy extends PCommonProxy {
    public static final ModelResourceLocation criteria = new ModelResourceLocation(new ResourceLocation(PInfo.MODPATH, "item"), "inventory");
    public static boolean bookLocked = true; //You can't edit me
    public static boolean isSaver = false;

    @Override
    public void initClient() {
        MinecraftForge.EVENT_BUS.register(new ItemProgressionRenderer());
       RemappingHandler.resetRegistries(true); //Create the registries on the client //
    }

    private ModelResourceLocation getLocation(String name) {
        return new ModelResourceLocation(new ResourceLocation(PInfo.MODPATH, name), "inventory");
    }

    @Override
    public void registerRendering() {
        RenderItemHelper.register(Progression.item, 0, criteria);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ProgressionOverride.INSTANCE, Progression.item);
        for (ItemProgression.ItemMeta meta: ItemProgression.ItemMeta.values()) {
            if (meta == ItemProgression.ItemMeta.criteria) continue;
            RenderItemHelper.register(Progression.item, meta.ordinal(), getLocation(meta.name()));
        }

        //Load Templates
        TemplateHandler.init();
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIDs.EDITOR) {
            if (bookLocked) {
                if (Options.editor) ChatHelper.displayChat(GOLD + translate("message.data"));
                else ChatHelper.displayChat(GOLD + translate("message.disabled"));
            } else return CORE.setEditor(CORE.lastGui);
        }  else if (ID == GuiIDs.GROUP) return CORE.setEditor(GROUP_EDITOR);


        return null;
    }
}