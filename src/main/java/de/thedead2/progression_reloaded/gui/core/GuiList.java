package de.thedead2.progression_reloaded.gui.core;

import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.gui.editors.*;
import de.thedead2.progression_reloaded.gui.editors.insert.FeatureNewCondition;
import de.thedead2.progression_reloaded.gui.editors.insert.FeatureNewFilter;
import de.thedead2.progression_reloaded.gui.editors.insert.FeatureNewReward;
import de.thedead2.progression_reloaded.gui.editors.insert.FeatureNewTrigger;
import de.thedead2.progression_reloaded.gui.filters.FeatureItemPreview;
import de.thedead2.progression_reloaded.json.JSONLoader;
import de.thedead2.progression_reloaded.json.Theme;
import de.thedead2.progression_reloaded.lib.PInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static de.thedead2.progression_reloaded.api.special.DisplayMode.DISPLAY;

public class GuiList {
    //Mode - > Default to Display
    public static DisplayMode MODE = DISPLAY;

    //Theme -> :O
    protected static final ResourceLocation resource = new ResourceLocation(PInfo.MODPATH, "config.json");
    public static Theme THEME = null;


    static {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            InputStream inputstream = null;
            try {
                IResource iresource = manager.getResource(resource);
                inputstream = iresource.getInputStream();
                THEME = JSONLoader.getGson().fromJson(IOUtils.toString(inputstream, "UTF-8"), Theme.class);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputstream != null) {
                    try {
                        inputstream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //Features -> Rules -> Create
    public static final FeatureNewTrigger NEW_TRIGGER = new FeatureNewTrigger();
    public static final FeatureNewCondition NEW_CONDITION = new FeatureNewCondition();
    public static final FeatureNewReward NEW_REWARD = new FeatureNewReward();
    public static final FeatureNewFilter NEW_FILTER = new FeatureNewFilter();

    //Features -> Rules -> List
    public static final FeatureTrigger TRIGGERS = new FeatureTrigger();
    public static final FeatureCondition CONDITIONS = new FeatureCondition();
    public static final FeatureReward REWARDS = new FeatureReward();
    public static final FeatureFilter FILTERS = new FeatureFilter();
    public static final FeatureItemSelectorTree TREE_ELEMENT = new FeatureItemSelectorTree();
    public static final FeatureTemplateSelectorTab TEMPLATE_SELECTOR_TAB = new FeatureTemplateSelectorTab();
    public static final FeatureTemplateSelectorCriteria TEMPLATE_SELECTOR_CRITERIA = new FeatureTemplateSelectorCriteria();

    //Features -> Editors
    public static final TextEditor TEXT_EDITOR_SIMPLE = new TextEditor();
    public static final FeatureFullTextEditor TEXT_EDITOR_FULL = new FeatureFullTextEditor();
    public static final FeatureItemSelector ITEM_EDITOR = new FeatureItemSelector();
    public static final FeatureItemPreview PREVIEW = new FeatureItemPreview();

    //Features -> Core
    public static final FeatureBackground BACKGROUND = new FeatureBackground();
    public static final FeatureBarsX2 FILTER_BG = new FeatureBarsX2("filter", "preview");
    public static final FeatureBarsX2 CRITERIA_BG = new FeatureBarsX2("trigger", "reward");
    public static final FeatureBarsX1 CONDITION_BG = new FeatureBarsX1("condition");
    public static final FeatureBarsFull GROUP_BG = new FeatureBarsFull("group");
    public static final FeatureFooter FOOTER = new FeatureFooter();
    public static final FeatureTooltip TOOLTIP = new FeatureTooltip();
    public static final FeatureLastDraw LAST = new FeatureLastDraw();

    //GUIs
    public static final GuiCore CORE = new GuiCore();
    public static final GuiGroupEditor GROUP_EDITOR = new GuiGroupEditor();
    public static final GuiTreeEditor TREE_EDITOR = new GuiTreeEditor();
    public static final GuiCriteriaEditor CRITERIA_EDITOR = new GuiCriteriaEditor();
    public static final GuiConditionEditor CONDITION_EDITOR = new GuiConditionEditor();
    public static final GuiFilterEditor FILTER_EDITOR = new GuiFilterEditor();
}
