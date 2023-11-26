package de.thedead2.progression_reloaded.client.gui.textures;

import net.minecraft.resources.ResourceLocation;

import static de.thedead2.progression_reloaded.util.ModHelper.MOD_ID;


public class IconInfos {

    public static final TextureInfo ALIGN_CENTER = createIconInfo("align_center.png");

    public static final TextureInfo ALIGN_LEFT = createIconInfo("align_left.png");

    public static final TextureInfo ALIGN_RIGHT = createIconInfo("align_right.png");

    public static final TextureInfo EDIT = createIconInfo("edit.png");

    public static final TextureInfo REDO = createIconInfo("redo.png");

    public static final TextureInfo UNDO = createIconInfo("undo.png");

    public static final TextureInfo BOLD = createIconInfo("bold.png");

    public static final TextureInfo ITALIC = createIconInfo("italic.png");

    public static final TextureInfo UNDERLINE = createIconInfo("underline.png");

    public static final TextureInfo STRIKETHROUGH = createIconInfo("strikethrough.png");

    public static final TextureInfo TEXT_COLOR = createIconInfo("text_color.png");

    public static final TextureInfo FONT_SIZE = createIconInfo("font_size.png");

    public static final TextureInfo INCREASE_FONT_SIZE = createIconInfo("increase_font_size.png");

    public static final TextureInfo DECREASE_FONT_SIZE = createIconInfo("decrease_font_size.png");

    public static final TextureInfo BULLET_LIST = createIconInfo("bullet_list.png");

    public static final TextureInfo NUMBERED_LIST = createIconInfo("numbered_list.png");

    public static final TextureInfo TRASH_CAN = createIconInfo("trash_can.png");

    public static final TextureInfo THEMES = createIconInfo("themes.png");

    public static final TextureInfo DRAG = createIconInfo("drag.png");


    private static TextureInfo createIconInfo(String name) {
        return new TextureInfo(new ResourceLocation(MOD_ID, "textures/gui/icons/" + name), 50, 50, true);
    }
}
