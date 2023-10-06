package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.ModRenderer;
import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public class ClientDisplayProgressToast implements ModNetworkPacket {

    private final IDisplayInfo displayInfo;

    @Nullable
    private final ResourceLocation levelId;


    public ClientDisplayProgressToast(IDisplayInfo displayInfo, @Nullable ResourceLocation levelId) {
        this.displayInfo = displayInfo;
        this.levelId = levelId;
    }


    @SuppressWarnings("unused")
    public ClientDisplayProgressToast(FriendlyByteBuf buf) {
        this.displayInfo = IDisplayInfo.deserializeFromNetwork(buf);
        this.levelId = buf.readNullable(FriendlyByteBuf::readResourceLocation);
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ModRenderer modRenderer = ModClientInstance.getInstance().getModRenderer();
                if(ClientDisplayProgressToast.this.levelId != null) {
                    modRenderer.displayNewLevelInfoScreen(ClientDisplayProgressToast.this.levelId);
                }

                modRenderer.addProgressCompleteToast(GuiFactory.createPRToast(ClientDisplayProgressToast.this.displayInfo));
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        this.displayInfo.serializeToNetwork(buf);
        buf.writeNullable(this.levelId, FriendlyByteBuf::writeResourceLocation);
    }
}
