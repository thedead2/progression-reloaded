package de.thedead2.progression_reloaded.criteria.filters.location;

import de.thedead2.progression_reloaded.api.criteria.ProgressionRule;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.IEnum;
import de.thedead2.progression_reloaded.api.special.IInit;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.entity.player.EntityPlayer;

@ProgressionRule(name="randomaround", color=0xFFBBBBBB)
public class FilterRandomAround extends FilterLocationBase implements IInit, ICustomDescription, IEnum {
    public LocationOperator xoperator = LocationOperator.RADIUS;
    public LocationOperator yoperator = LocationOperator.RADIUS;
    public LocationOperator zoperator = LocationOperator.RADIUS;
    public int dimensionID = 0;
    public int xCoordinate = 8;
    public int yCoordinate = 8;
    public int zCoordinate = 8;
    public int randomX = 32;
    public int randomY = 32;
    public int randomZ = 32;

    private transient FilterRandomX dummyX = new FilterRandomX();
    private transient FilterRandomY dummyY = new FilterRandomY();
    private transient FilterRandomZ dummyZ = new FilterRandomZ();

    @Override
    public void init(boolean isClient) {
        dummyX.operator = xoperator;
        dummyX.coordinate = xCoordinate;
        dummyX.distance = randomX;
        dummyY.operator = yoperator;
        dummyY.coordinate = yCoordinate;
        dummyY.distance = randomY;
        dummyZ.operator = zoperator;
        dummyZ.coordinate = zCoordinate;
        dummyZ.distance = randomZ;
    }

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.format("filter.location.randomaround.description", xCoordinate, yCoordinate, zCoordinate);
    }

    @Override
    public WorldLocation getRandom(EntityPlayer player) {
        int randX = dummyX.getRandom(player).pos.getX();
        int randY = dummyY.getRandom(player).pos.getY();
        int randZ = dummyZ.getRandom(player).pos.getZ();
        return new WorldLocation(dimensionID, randX, randY, randZ);
    }

    @Override
    public boolean matches(WorldLocation location) {
        if (location.dimension != dimensionID) return false;
        if (!dummyX.matches(location)) return false;
        if (!dummyY.matches(location)) return false;
        if (dummyZ.matches(location)) return false;
        return true; //Now we return true as we aren't outside the defined boundary
    }


    @Override
    public Enum next(String name) {
        int id = xoperator.ordinal() + 1;
        if (name.equals("yoperator")) id = yoperator.ordinal() + 1;
        else if (name.equals("zoperator")) id = zoperator.ordinal() + 1;

        if (id < LocationOperator.values().length) {
            return LocationOperator.values()[id];
        }

        return LocationOperator.values()[0];
    }

    @Override
    public boolean isEnum(String name) {
        return name.contains("operator");
    }
}
