package de.thedead2.progression_reloaded.helpers;

import net.minecraft.world.DimensionType;

public class DimensionHelper {
    public static String getDimensionNameFromID(final int id) {
        try {
             DimensionType type = DimensionType.getById(id);
            if (type != null) return type.getName();
        } catch (Exception e) {}

        return "Invalid Dimension";
    }
}
