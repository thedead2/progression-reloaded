package de.thedead2.progression_reloaded.client.gui.themes;

import com.google.common.base.Preconditions;
import de.thedead2.progression_reloaded.client.gui.fonts.formatting.FontFormatting;
import de.thedead2.progression_reloaded.client.gui.textures.TextureInfo;
import de.thedead2.progression_reloaded.client.gui.util.ObjectFit;
import de.thedead2.progression_reloaded.client.gui.util.TooltipInfo;
import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public record ProgressionTheme(int ordinal, TextureInfo logo, TextureInfo toast, TextureInfo progressBarEmpty, TextureInfo progressBarFilled, TextureInfo backgroundFrame, TextureInfo questWidgetHovered, TooltipInfo tooltip, ResourceLocation font, ResourceLocation layout) {

    public static class Builder {

        private final String locationsPath;

        private ResourceLocation layout;

        private int ordinal;

        private TextureInfo logo, toast, progressBarEmpty, progressBarFilled, backgroundFrame, questWidget;

        private TooltipInfo tooltip;

        private ResourceLocation font = new ResourceLocation("default");


        private Builder(String locationsPath) {
            this.locationsPath = locationsPath;
        }


        public static Builder builder(String locationsPath) {
            return new Builder(locationsPath);
        }


        public Builder withLogo(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit) {
            this.logo = new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit);
            return this;
        }


        private ResourceLocation createId(String name) {
            return new ResourceLocation(ModHelper.MOD_ID, locationsPath + name);
        }


        public Builder withToast(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit) {
            this.toast = new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit);
            return this;
        }


        public Builder withProgressBarEmpty(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit) {
            this.progressBarEmpty = new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit);
            return this;
        }


        public Builder withProgressBarFilled(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit) {
            this.progressBarFilled = new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit);
            return this;
        }


        public Builder withBackgroundFrame(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit) {
            this.backgroundFrame = new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit);
            return this;
        }


        public Builder withQuestWidget(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit) {
            this.questWidget = new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit);
            return this;
        }


        public Builder withFont(ResourceLocation font) {
            this.font = font;
            return this;
        }


        public Builder withTooltip(String fileName, Component altText, float u, float v, float textureWidth, float textureHeight, ObjectFit objectFit, FontFormatting formatting) {
            this.tooltip = new TooltipInfo(new TextureInfo(createId(fileName), altText, u, v, textureWidth, textureHeight, objectFit), formatting.setFont(this.font));
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
            Preconditions.checkNotNull(questWidget);
            Preconditions.checkNotNull(tooltip);
            this.tooltip.formatting().setFont(this.font);

            return new ProgressionTheme(ordinal, logo, toast, progressBarEmpty, progressBarFilled, backgroundFrame, questWidget, tooltip, font, layout);
        }
    }
}
