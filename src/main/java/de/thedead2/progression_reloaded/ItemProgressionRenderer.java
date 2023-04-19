package de.thedead2.progression_reloaded;

import com.google.common.collect.ImmutableList;
import de.thedead2.progression_reloaded.api.criteria.ICriteria;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static de.thedead2.progression_reloaded.ItemProgression.getCriteriaFromStack;

public class ItemProgressionRenderer implements IBakedModel  {
    @SubscribeEvent
    public void onCookery(ModelBakeEvent event) {
        event.getModelRegistry().putObject(PClientProxy.criteria, this);
    }

    /** Redundant crap below :/ **/
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ProgressionOverride.INSTANCE;
    }

    protected static class ProgressionOverride extends ItemOverrideList implements IItemColor {
        public static ProgressionOverride INSTANCE = new ProgressionOverride();
        private ItemStack broken = new ItemStack(Items.BOOK);
        private ItemModelMesher mesher;

        public ProgressionOverride() {
            super(ImmutableList.<ItemOverride>of());
        }

        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            ICriteria criteria = getCriteriaFromStack(stack, true);
            if (criteria != null) {
                return Minecraft.getMinecraft().getItemColors().getColorFromItemstack(criteria.getIcon(), tintIndex);
            }

            return 16777215;
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            if (mesher == null) mesher  = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
            ICriteria criteria = getCriteriaFromStack(stack, true);
            if (criteria != null && criteria.getIcon().getItem() != stack.getItem() && criteria.getIcon().getItem() != Progression.item) {
                return mesher.getItemModel(criteria.getIcon());
            }

            return mesher.getItemModel(broken);
        }
    }
}