package de.thedead2.progression_reloaded.client.gui.components.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.animation.AnimationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.InterpolationTypes;
import de.thedead2.progression_reloaded.client.gui.animation.LoopTypes;
import de.thedead2.progression_reloaded.client.gui.animation.SimpleAnimation;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.util.RenderUtil;
import de.thedead2.progression_reloaded.util.helper.MathHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.client.gui.components.Renderable;
import org.jetbrains.annotations.NotNull;


public class Cursor implements Renderable {

    private final SimpleAnimation blinkAnimation = new SimpleAnimation(MathHelper.secondsToTicks(0.65f / 2), MathHelper.secondsToTicks(0.65f), LoopTypes.LOOP, AnimationTypes.STEPS(1), InterpolationTypes.LINEAR);

    private final int lineWidth = 2;

    private final Int2ObjectFunction<FontFormatting> format;

    private float xPos;

    private float yPos;

    private float zPos;

    private int charPos;

    private int selectPos;

    private float alpha = 1;


    public Cursor(float xPos, float yPos, float zPos, int charPos, Int2ObjectFunction<FontFormatting> format) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.charPos = charPos;
        this.format = format;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.blinkAnimation.animate(0, this.alpha, t -> RenderUtil.verticalLine(poseStack, this.xPos, this.yPos - 2, this.yPos + this.getHeight() - 2, this.zPos, lineWidth, RenderUtil.changeAlpha(this.getFormat().getColor(), t)));
    }


    public float getHeight() {
        return this.getFormat().getLineHeight() + 4;
    }

    public FontFormatting getFormat() {
        return this.format.get(this.charPos - 1);
    }


    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }


    public int getCharPos() {
        return charPos;
    }


    public void setCharPos(int i) {
        this.charPos = i;
    }


    public int getSelectPos() {
        return selectPos;
    }


    public void setSelectPos(int selectPos) {
        this.selectPos = selectPos;
    }


    public void moveCharPos(int amount) {
        this.charPos += amount;
    }


    public void setSelectToCurrentPos() {
        this.selectPos = this.charPos;
    }


    public boolean hasSelection() {
        return this.selectPos != this.charPos;
    }


    public void setDisplayPos(float x, float y, float z) {
        this.xPos = x;
        this.yPos = y;
        this.zPos = z;
    }


    public float getLineWidth() {
        return lineWidth;
    }


    public float getXPos() {
        return xPos;
    }


    public float getYPos() {
        return yPos;
    }


    public float getZPos() {
        return zPos;
    }
}
