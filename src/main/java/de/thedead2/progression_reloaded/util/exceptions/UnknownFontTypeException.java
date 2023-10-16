package de.thedead2.progression_reloaded.util.exceptions;

import net.minecraft.resources.ResourceLocation;

import java.io.IOException;


public class UnknownFontTypeException extends IOException {
    public UnknownFontTypeException(ResourceLocation fontId) {
        super("Unknown font type: " + fontId.toString());
    }
}
