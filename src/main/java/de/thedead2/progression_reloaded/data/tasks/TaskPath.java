package de.thedead2.progression_reloaded.data.tasks;

import de.thedead2.progression_reloaded.util.ModHelper;
import net.minecraft.resources.ResourceLocation;
import stdlib.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TaskPath {

    private final List<ResourceLocation> components = new ArrayList<>();


    public TaskPath(ResourceLocation... ids) {
        this.components.addAll(List.of(ids));
    }


    public TaskPath(String path) {
        String[] strings = Strings.split(path, '/');
        this.components.addAll(Arrays.stream(strings).map(s -> new ResourceLocation(ModHelper.MOD_ID, s)).toList());
    }


    public void append(ResourceLocation id) {
        this.components.add(id);
    }


    public boolean remove(ResourceLocation id) {
        return this.components.remove(id);
    }


    public void clear() {
        this.components.clear();
    }


    public ResourceLocation getLast() {
        return this.components.get(this.components.size() - 1);
    }


    public ResourceLocation getFirst() {
        return this.components.get(0);
    }


    public ResourceLocation get(int i) {
        return this.components.get(i);
    }


    public TaskPath getParent() {
        TaskPath path = new TaskPath();
        path.components.addAll(this.components.subList(0, this.components.size() - 1));

        return path;
    }


    public TaskPath subPath(int beginIndex, int endIndex) {
        TaskPath path = new TaskPath();
        path.components.addAll(this.components.subList(beginIndex, endIndex));

        return path;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(ResourceLocation id : components) {
            builder.append(id.getPath()).append("/");
        }

        return builder.toString();
    }
}
