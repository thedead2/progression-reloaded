package de.thedead2.progression_reloaded.data;

import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;

public class ProgressionLevel {

    private final int index;
    private final String name;
    private final ResourceLocation id;

    private final boolean baseLevel;

    private final ProgressionLevel previousLevel;

    private static final ProgressionLevel LOWEST = new ProgressionLevel("base", new ResourceLocation(ModHelper.MOD_ID, "base_level"));

    public ProgressionLevel(int index, String name, ResourceLocation id, boolean baseLevel, ProgressionLevel previousLevel) {
        this.index = index;
        this.name = name;
        this.id = id;
        this.baseLevel = baseLevel;
        this.previousLevel = previousLevel;
    }

    public ProgressionLevel(String name, ResourceLocation id) {
        this(0, name, id, true, null);
    }

    public static ProgressionLevel fromKey(ResourceLocation level) {
        return lowest();
    }

    public static ProgressionLevel lowest() {
        return LOWEST;
    }


    public boolean contains(ProgressionLevel other) {
        if(this.equals(other) || (!this.baseLevel && previousLevel.equals(other))) return true;
        else if(this.baseLevel) return false;
        else return previousLevel.contains(other);
    }

    public ProgressionLevel getPreviousLevel() {
        return previousLevel;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public static class Builder{

        private String name;

        private Builder(){
        }

        public static Builder builder(){
            return new Builder();
        }

        public void addName(String name){
            this.name = name;
        }

        public ProgressionLevel build(){
            return lowest();
        }
    }
}
