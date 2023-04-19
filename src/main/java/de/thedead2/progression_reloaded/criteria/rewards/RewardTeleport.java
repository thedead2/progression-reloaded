package de.thedead2.progression_reloaded.criteria.rewards;

import com.google.common.collect.Lists;
import de.thedead2.progression_reloaded.api.criteria.*;
import de.thedead2.progression_reloaded.api.special.DisplayMode;
import de.thedead2.progression_reloaded.api.special.ICustomDescription;
import de.thedead2.progression_reloaded.api.special.IHasFilters;
import de.thedead2.progression_reloaded.api.special.ISpecialFieldProvider;
import de.thedead2.progression_reloaded.gui.fields.ItemFilterField;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeEntity;
import de.thedead2.progression_reloaded.gui.filters.FilterTypeLocation;
import de.thedead2.progression_reloaded.helpers.EntityHelper;
import de.thedead2.progression_reloaded.lib.WorldLocation;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

@ProgressionRule(name="teleport", color=0xFFDDDDDD, icon="minecraft:ender_pearl")
public class RewardTeleport extends RewardBase implements ICustomDescription, IHasFilters, ISpecialFieldProvider {
    public List<IFilterProvider> locations = new ArrayList();
    public List<IFilterProvider> targets = new ArrayList();
    public boolean defaultToPlayer = true;
    protected transient IField field;

    public RewardTeleport() {
        field = new ItemFilterField("locations", this);
    }

    @Override
    public String getDescription() {
        return de.thedead2.progression_reloaded.ProgressionReloaded.translate(getProvider().getUnlocalisedName() + ".description") + " \n" + field.getField();
    }

    @Override
    public List<IFilterProvider> getAllFilters() {
        List<IFilterProvider> list = Lists.newArrayList();
        list.addAll(locations);
        list.addAll(targets);
        return list;
    }

    @Override
    public IFilterType getFilterForField(String fieldName) {
        if (fieldName.equals("targets")) return FilterTypeEntity.INSTANCE;
        return FilterTypeLocation.INSTANCE;
    }

    @Override
    public void addSpecialFields(List<IField> fields, DisplayMode mode) {
        fields.add(new ItemFilterField("locations", this));
        fields.add(new ItemFilterField("targets", this));
    }

    @Override
    public void reward(EntityPlayerMP thePlayer) {
        boolean notteleported = true;
        for (int i = 0; i < 10 && notteleported; i++) {
            WorldLocation location = WorldLocation.getRandomLocationFromFilters(locations, thePlayer);
            if (location != null) {
                IFilter filter = EntityHelper.getFilter(targets, thePlayer);
                if (filter != null) {
                    List<EntityLivingBase> entities = (List<EntityLivingBase>) filter.getRandom(thePlayer);
                    if (entities.size() == 0 && defaultToPlayer) entities.add(thePlayer);
                    for (EntityLivingBase entity : entities) {
                        World world = DimensionManager.getWorld(location.dimension);
                        int dimension = location.dimension;
                        if (world == null) continue; //NO!!!!
                        if (entity.dimension != dimension) { //From RFTools
                            MinecraftServer server = entity.worldObj.getMinecraftServer();
                            WorldServer worldServer = server.worldServerForDimension(dimension);
                            int oldDimension = entity.worldObj.provider.getDimension();
                            BlockPos pos = new BlockPos(location.pos);
                            if (entity instanceof EntityPlayer) {
                                ((EntityPlayerMP)entity).addExperienceLevel(0); //Fix levels
                                worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(((EntityPlayerMP)entity), dimension, new DimensionTeleportation(worldServer, new BlockPos(location.pos)));
                            } else entity.changeDimension(dimension);

                            entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D);
                            if (oldDimension == 1) {
                                entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D);
                                world.spawnEntityInWorld(entity);
                                world.updateEntityWithOptionalForce(entity, false);
                            }

                            notteleported = false;
                        } else {
                            BlockPos pos = new BlockPos(location.pos);
                            if (world.isBlockLoaded(pos)) {
                                if (isValidLocation(world, pos)) {
                                    notteleported = false;
                                    entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //Helper Methods
    private boolean isValidLocation(World world, BlockPos pos) {
        IBlockState floorState = world.getBlockState(pos);
        IBlockState feetState = world.getBlockState(pos.up());
        IBlockState headState = world.getBlockState(pos.up(2));
        Material floor = floorState.getBlock().getMaterial(floorState);
        Material feet = feetState.getBlock().getMaterial(feetState);
        Material head = headState.getBlock().getMaterial(headState);
        if (feet.blocksMovement()) return false;
        if (head.blocksMovement()) return false;
        if (floor.isLiquid() || feet.isLiquid() || head.isLiquid()) return false;
        return floor.blocksMovement();
    }

    public static class DimensionTeleportation extends Teleporter {
        private final WorldServer world;
        private final BlockPos pos;

        public DimensionTeleportation(WorldServer world, BlockPos pos) {
            super(world);
            this.world = world;
            this.pos = pos;
        }

        @Override
        public void placeInPortal(Entity entity, float rotationYaw) {
            world.getBlockState(pos);
            entity.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY() + 1, pos.getZ() + 0.5D);
            entity.motionX = 0.0f;
            entity.motionY = 0.0f;
            entity.motionZ = 0.0f;
        }
    }
}