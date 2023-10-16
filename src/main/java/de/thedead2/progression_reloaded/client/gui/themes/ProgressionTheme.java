package de.thedead2.progression_reloaded.client.gui.themes;

import com.google.common.base.Preconditions;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;


public record ProgressionTheme(int ordinal, TextureInfo logo, TextureInfo toast, TextureInfo progressBarEmpty, TextureInfo progressBarFilled, TextureInfo backgroundFrame, ResourceLocation font, ResourceLocation layout) {

    public static class Builder {

        private final String locationsPath;

        private ResourceLocation layout;

        private int ordinal;

        private TextureInfo logo, toast, progressBarEmpty, progressBarFilled, backgroundFrame;

        private ResourceLocation font = new ResourceLocation("default");


        private Builder(String locationsPath) {
            this.locationsPath = locationsPath;
        }


        public static Builder builder(String locationsPath) {
            return new Builder(locationsPath);
        }


        public Builder withLogo(String fileName, float u, float v, float textureWidth, float textureHeight) {
            this.logo = new TextureInfo(createId(fileName), u, v, textureWidth, textureHeight);
            return this;
        }


        private ResourceLocation createId(String name) {
            return new ResourceLocation(ModHelper.MOD_ID, locationsPath + name);
        }


        public Builder withToast(String fileName, float u, float v, float textureWidth, float textureHeight) {
            this.toast = new TextureInfo(createId(fileName), u, v, textureWidth, textureHeight);
            return this;
        }


        public Builder withProgressBarEmpty(String fileName, float u, float v, float textureWidth, float textureHeight) {
            this.progressBarEmpty = new TextureInfo(createId(fileName), u, v, textureWidth, textureHeight);
            return this;
        }


        public Builder withProgressBarFilled(String fileName, float u, float v, float textureWidth, float textureHeight) {
            this.progressBarFilled = new TextureInfo(createId(fileName), u, v, textureWidth, textureHeight);
            return this;
        }


        public Builder withBackgroundFrame(String fileName, float u, float v, float textureWidth, float textureHeight) {
            this.backgroundFrame = new TextureInfo(createId(fileName), u, v, textureWidth, textureHeight);
            return this;
        }


        public Builder withFont(ResourceLocation font) {
            this.font = font;
            return this;
        }


        public Builder withLayout(ResourceLocation layout) {
            this.layout = layout;
            return this;
        }


        public Builder withOrdinal(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }


        public ProgressionTheme build() {
            Preconditions.checkNotNull(layout);
            Preconditions.checkNotNull(logo);
            Preconditions.checkNotNull(toast);
            Preconditions.checkNotNull(progressBarEmpty);
            Preconditions.checkNotNull(progressBarFilled);
            Preconditions.checkNotNull(backgroundFrame);

            return new ProgressionTheme(ordinal, logo, toast, progressBarEmpty, progressBarFilled, backgroundFrame, font, layout);
        }
    }
}
