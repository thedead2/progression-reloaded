package joshie.progression.json;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import joshie.progression.lib.ProgressionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class Theme {
    protected static final ResourceLocation resource = new ResourceLocation(ProgressionInfo.MODPATH, "config.json");

    public static Theme INSTANCE = new Theme();

    public int invisible = 0x00000000;

    public int criteriaDisplayNameColor = 0xFFFFFFFF;
    public int criteriaEditDisplayNameColor = 0xFFFFFFFF;
    public int backgroundColor = 0xEE121212;
    public int backgroundBorder = 0xFFFFFFFF;

    public int connectLineColor1 = 0xDDB9B9AD;
    public int connectLineColor2 = 0xFF636C69;
    public int connectLineColor3 = 0xFFE8EFE7;
    
    public int toolTipWhite = 0xFFFFFFFF;

    public int optionsFontColor = 0xFFFFFFFF;
    public int optionsFontColorHover = 0xFFBBBBBB;

    public int newButtonFontColor = 0xFFFFFFFF;

    public int triggerBoxGradient1 = 0xFF0080FF;
    public int triggerBoxGradient2 = 0xFF00468C;
    public int triggerBoxUnderline1 = 0xFF003366;
    public int triggerBoxFont = 0xFFFFFFFF;

    public int rewardBoxGradient1 = 0xFFB20000;
    public int rewardBoxGradient2 = 0xFF660000;
    public int rewardBoxBorder = 0xFF660000;
    public int rewardBoxFont = 0xFFFFFFFF;

    //Black Bars
    public int blackBarFontColor = 0xFFFFFFFF;
    public int blackBarGradient1 = 0xFF222222;
    public int blackBarGradient2 = 0xFF000000;
    public int blackBarBorder = 0xFF000000;
    public int blackBarUnderLine = 0xFF000000;
    public int blackBarUnderLineBorder = 0xFFFFFFFF;

    public int scrollTextFontColor = 0xFFFFFFFF;

    //Editor
    public int conditionEditorFont = 0xFFFFFFFF;
    public int conditionEditorGradient1 = 0xFFFF8000;
    public int conditionEditorGradient2 = 0xFFB25900;
    public int conditionEditorBorder = 0xFF8C4600;
    public int conditionEditorUnderline = 0xFF8C4600;
    public int conditionEditorUnderline2 = 0xFFFFFFFF;

    //Condition Colors
    public int conditionFontColor = 0xFFFFFFFF;
    public int conditionGradient1 = 0xFF222222;
    public int conditionGradient2 = 0xFF000000;

    //Trigger Colors
    public int triggerFontColor = 0xFFFFFFFF;
    public int triggerGradient1 = 0xFF222222;
    public int triggerGradient2 = 0xFF000000;

    public int newBox1 = 0xDD000000;
    public int newBox2 = 0xFF000000;

    public int newGradient1 = 0xFFFF8000;
    public int newGradient2 = 0xFF8C4600;
    public int newBorder = 0xFF000000;
    public int newFont = 0xFFFFFFFF;
    public int newFontHover = 0xFF2693FF;

    static {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            InputStream inputstream = null;
            try {
                IResource iresource = manager.getResource(resource);
                inputstream = iresource.getInputStream();
                INSTANCE = JSONLoader.gson.fromJson(IOUtils.toString(inputstream, "UTF-8"), Theme.class);
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
}
