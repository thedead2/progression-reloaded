package de.thedead2.progression_reloaded.util.helper;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationHelper {

    public static ResourceLocation getOrDefault(String id, ResourceLocation defaultVal){
        try {
            return new ResourceLocation(id);
        }
        catch (ResourceLocationException e){
            return defaultVal;
        }
    }
    public static ResourceLocation getOrDefault(String namespace, String path, ResourceLocation defaultVal){
        if(namespace.contains(":")) return getOrDefault(namespace, defaultVal);
        else if (path.contains(":")) return getOrDefault(path, defaultVal);
        try {
            return new ResourceLocation(namespace, path);
        }
        catch (ResourceLocationException e){
            return defaultVal;
        }
    }
}
