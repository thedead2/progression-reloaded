package de.thedead2.progression_reloaded.data;

public class ProgressionLevel {

    private final int index;
    private final String name;

    private final boolean baseLevel;

    private final ProgressionLevel previousLevel;

    public ProgressionLevel(int index, String name, boolean baseLevel, ProgressionLevel previousLevel) {
        this.index = index;
        this.name = name;
        this.baseLevel = baseLevel;
        this.previousLevel = previousLevel;
    }

    public ProgressionLevel(String name) {
        this(0, name, true, null);
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
            return new ProgressionLevel(name);
        }
    }
}
