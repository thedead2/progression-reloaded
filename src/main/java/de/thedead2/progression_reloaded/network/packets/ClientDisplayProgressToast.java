package de.thedead2.progression_reloaded.network.packets;

import de.thedead2.progression_reloaded.api.gui.IDisplayInfo;
import de.thedead2.progression_reloaded.api.network.ModNetworkPacket;
import de.thedead2.progression_reloaded.client.ModClientInstance;
import de.thedead2.progression_reloaded.client.gui.GuiFactory;
import de.thedead2.progression_reloaded.client.gui.components.toasts.NotificationToast;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientDisplayProgressToast implements ModNetworkPacket {

    private final IDisplayInfo<?> displayInfo;

    private final Component title;

    private final boolean checkIfNotFollowed;


    public ClientDisplayProgressToast(IDisplayInfo<?> displayInfo, Component title) {
        this(displayInfo, title, false);
    }


    public ClientDisplayProgressToast(IDisplayInfo<?> displayInfo, Component title, boolean checkIfNotFollowed) {
        this.displayInfo = displayInfo;
        this.title = title;
        this.checkIfNotFollowed = checkIfNotFollowed;
    }


    @SuppressWarnings("unused")
    public ClientDisplayProgressToast(FriendlyByteBuf buf) {
        this.displayInfo = IDisplayInfo.deserializeFromNetwork(buf);
        this.title = buf.readComponent();
        this.checkIfNotFollowed = buf.readBoolean();
    }


    @Override
    @SuppressWarnings("Convert2Lambda")
    public DistExecutor.SafeRunnable onClient(Supplier<NetworkEvent.Context> ctx) {
        return new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                boolean bool = true;
                if(checkIfNotFollowed) {
                    bool = !ModClientInstance.getInstance().getModRenderer().isQuestFollowed(displayInfo.getId());
                }

                if(bool) {
                    ModClientInstance.getInstance().getModRenderer().getToastRenderer().scheduleToastDisplay(NotificationToast.Priority.LOW, GuiFactory.createProgressToast(ClientDisplayProgressToast.this.displayInfo, title));
                }
            }
        };
    }


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        this.displayInfo.serializeToNetwork(buf);
        buf.writeComponent(this.title);
        buf.writeBoolean(this.checkIfNotFollowed);
    }
}
