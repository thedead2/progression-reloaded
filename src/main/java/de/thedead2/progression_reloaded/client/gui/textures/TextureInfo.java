package de.thedead2.progression_reloaded.client.gui.textures;

import net.minecraft.resources.ResourceLocation;


public class TextureInfo {

    /**
     * The location for the texture
     */
    private final ResourceLocation textureLocation;

    /**
     * The u start position inside the texture file
     */
    private final float u;

    /**
     * The v start position inside the texture file
     */
    private final float v;

    private final float textureWidth;

    private final float textureHeight;

    private final float aspectRatio;

    private final float[] colorShift = new float[]{1.0f, 1.0f, 1.0f, 1.0f};


    public TextureInfo(ResourceLocation textureLocation, float textureWidth, float textureHeight) {
        this(textureLocation, 0, 0, textureWidth, textureHeight);
    }


    public TextureInfo(ResourceLocation textureLocation, float u, float v, float textureWidth, float textureHeight) {
        this.textureLocation = textureLocation;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.aspectRatio = textureWidth / textureHeight;
    }


    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }


    public float getU() {
        return u;
    }


    public float getV() {
        return v;
    }


    public float getTextureWidth() {
        return textureWidth;
    }


    public float getTextureHeight() {
        return textureHeight;
    }


    public float getAspectRatio() {
        return aspectRatio;
    }


    /**
     * @return the relative width of the texture to the given height
     **/
    public float getRelativeWidth(float height) {
        return height * aspectRatio;
    }


    /**
     * @return the relative height of the texture to the given width
     **/
    public float getRelativeHeight(float width) {
        return width / aspectRatio;
    }


    public float[] getColorShift() {
        return colorShift;
    }
}
