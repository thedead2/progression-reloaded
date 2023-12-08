package de.thedead2.progression_reloaded.events;

import de.thedead2.progression_reloaded.api.gui.fonts.IFontReader;
import de.thedead2.progression_reloaded.client.gui.themes.ProgressionTheme;
import de.thedead2.progression_reloaded.client.gui.themes.layouts.ProgressionLayout;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;


public abstract class RegisterEvent<T> extends Event {

    private final Map<ResourceLocation, T> objects = new HashMap<>();


    public void accept(ResourceLocation id, T object) {
        if(this.objects.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate object registration with uuid: " + id);
        }
        this.objects.put(id, object);
    }


    Map<ResourceLocation, T> getObjects() {
        return this.objects;
    }

    public static class RegisterThemesEvent extends RegisterEvent<ProgressionTheme> {}
    public static class RegisterLayoutsEvent extends RegisterEvent<ProgressionLayout> {}
    public static class RegisterFontTypesEvent extends RegisterEvent<IFontReader<?>> {}

}
