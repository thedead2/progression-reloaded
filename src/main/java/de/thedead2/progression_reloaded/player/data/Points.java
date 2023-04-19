package de.thedead2.progression_reloaded.player.data;

import de.thedead2.progression_reloaded.helpers.NBTHelper;
import de.thedead2.progression_reloaded.network.core.PacketNBT.INBTWritable;
import de.thedead2.progression_reloaded.player.nbt.PointsNBT;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

public class Points implements INBTWritable<Points> {
    private HashMap<String, Double> points = new HashMap();

    public double getDouble(String name) {
        String key = "points:" + name;
        double amount = 0;
        if (points.containsKey(key)) {
            amount = points.get(key);
        } else points.put(key, amount);

        return amount;
    }
    
    public boolean getBoolean(String name) {
        String key = "boolean:" + name;
        double amount = 0;
        if (points.containsKey(key)) {
            amount = points.get(key);
        } else points.put(key, amount);
        
        return amount == 0 ? false : true;
    }
    
    public void setDouble(String name, double points) {
        this.points.put("points:" + name, points);
    }
    
    public void setBoolean(String name, boolean value) {
        double number = value == true ? 1D : 0D;
        this.points.put("boolean:" + name, number);
    }

    @Override
    public Points readFromNBT(NBTTagCompound tag) {
        NBTHelper.readMap(tag, "Points", PointsNBT.INSTANCE.setMap(points));
        return this;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTHelper.writeMap(tag, "Points", PointsNBT.INSTANCE.setMap(points));
        return tag;
    }
}
