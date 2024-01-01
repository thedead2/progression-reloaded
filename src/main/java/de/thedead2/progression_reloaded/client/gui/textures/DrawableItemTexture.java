package de.thedead2.progression_reloaded.client.gui.textures;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.thedead2.progression_reloaded.client.gui.components.ScreenComponent;
import de.thedead2.progression_reloaded.client.gui.util.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


//FIXME: When not in gui debug mode, item always renders on top of other components
public class DrawableItemTexture extends ScreenComponent {

    private final ItemStack item;


    public DrawableItemTexture(Area area, ItemStack item) {
        super(area);
        this.item = item;
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(item, null, null, 0);
        minecraft.textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack modelViewStack = getPoseStack(itemRenderer, poseStack);
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean usesBlockLight = !bakedModel.usesBlockLight();
        if(usesBlockLight) {
            Lighting.setupForFlatItems();
        }

        /*minecraft.font.drawShadow(poseStack, "This is a test!", this.area.getX(), this.area.getY(), Color.GREEN.getRGB());*/

        itemRenderer.render(item, ItemTransforms.TransformType.GUI, false, new PoseStack(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if(usesBlockLight) {
            Lighting.setupFor3DItems();
        }

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }


    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.item.getDisplayName());
    }


    @NotNull
    private PoseStack getPoseStack(ItemRenderer itemRenderer, PoseStack poseStack) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(poseStack.last().pose());

        modelViewStack.translate(this.area.getInnerX() + (this.area.getInnerWidth() / 2), this.area.getInnerY() + (this.area.getInnerHeight() / 2), this.area.getZ() + itemRenderer.blitOffset);
        modelViewStack.scale(1.0F, -1.0F, 1.0F);
        modelViewStack.scale(this.area.getInnerWidth(), this.area.getInnerHeight(), 1);
        return modelViewStack;
    }
}
