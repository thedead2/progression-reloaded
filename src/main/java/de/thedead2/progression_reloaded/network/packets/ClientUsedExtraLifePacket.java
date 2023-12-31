package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientUsedExtraLifePacket implements ModNetworkPacket {

    private final int entityId;


    public ClientUsedExtraLifePacket(Player player) {
        this.entityId = player.getId();
    }


    @SuppressWarnings("unused")
    public ClientUsedExtraLifePacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                Minecraft minecraft = Minecraft.getInstance();
                Level level = minecraft.level;
                Entity entity = level.getEntity(entityId);
                minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
                ModClientInstance.getInstance().getModRenderer().displayExtraLifeAnimation();
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }
}
