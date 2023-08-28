package de.thedead2.progression_reloaded.items.custom;

import de.thedead2.progression_reloaded.network.ModNetworkHandler;
import de.thedead2.progression_reloaded.network.packages.ClientOpenProgressionBookPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ProgressionBookItem extends Item {

    public ProgressionBookItem() {
        super(new Properties().stacksTo(1).fireResistant());
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if(!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
            ModNetworkHandler.sendToPlayer(new ClientOpenProgressionBookPacket(), serverPlayer);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        if(Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.literal("Open for your progression").withStyle(ChatFormatting.GOLD));
        }
    }

    /*@Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                class ProgressionBookItemRenderer extends BlockEntityWithoutLevelRenderer{

                    public ProgressionBookItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
                        super(pBlockEntityRenderDispatcher, pEntityModelSet);
                    }

                    @Override
                    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
                        super.renderByItem(pStack, pTransformType, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
                    }
                }
                return new ProgressionBookItemRenderer(, new EntityModelSet());
            }
        });
    }*/
}
