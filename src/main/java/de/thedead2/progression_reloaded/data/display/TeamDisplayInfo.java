package de.thedead2.progression_reloaded.data.display;

import com.google.common.collect.Lists;
import de.thedead2.progression_reloaded.player.types.KnownPlayer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TeamDisplayInfo {

    private final String name;

    private final List<KnownPlayer> members = new ArrayList<>();


    public TeamDisplayInfo(String name, Collection<KnownPlayer> members) {
        this.name = name;
        this.members.addAll(members);
    }


    public static TeamDisplayInfo fromNetwork(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        List<KnownPlayer> members = buf.readCollection(Lists::newArrayListWithExpectedSize, KnownPlayer::fromNetwork);

        return new TeamDisplayInfo(name, members);
    }


    public String getName() {
        return name;
    }


    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUtf(this.name);
        buf.writeCollection(this.members, (buf1, player) -> player.toNetwork(buf1));
    }
}
