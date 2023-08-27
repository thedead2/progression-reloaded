package de.thedead2.progression_reloaded.client.gui.themes;

import de.thedead2.progression_reloaded.client.gui.util.objects.ImageRenderObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record ProgressionTheme(/*@Nullable ResourceLocation background, ResourceLocation mainFrame, ResourceLocation logo, ResourceLocation questFrame, Color mainColor*/) {

    record Color(float red, float blue, float green, float alpha){}
}
