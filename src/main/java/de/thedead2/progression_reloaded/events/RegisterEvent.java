package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public abstract class RegisterEvent<T> extends PREventFactory.ProgressionEvent {

    private final Map<ResourceLocation, T> objects = new HashMap<>();


    public void accept(ResourceLocation id, T object) {
        if(this.objects.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate object registration with id: " + id);
        }
        this.objects.put(id, object);
    }


    Map<ResourceLocation, T> getObjects() {
        return this.objects;
    }


    public static class RegisterThemesEvent extends RegisterEvent<ProgressionTheme> {

    }

    public static class RegisterLayoutsEvent extends RegisterEvent<ProgressionLayout> {

    }
}
