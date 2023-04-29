package de.thedead2.progression_reloaded.player;

import com.google.common.base.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record KnownPlayer(ResourceLocation id, String name) {

    public static KnownPlayer fromPlayer(Player player){
        return new KnownPlayer(SinglePlayer.createId(player.getStringUUID()), player.getScoreboardName());
    }

    public static KnownPlayer fromSinglePlayer(SinglePlayer singlePlayer){
        return new KnownPlayer(singlePlayer.getId(), singlePlayer.getPlayerName());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnownPlayer that = (KnownPlayer) o;
        return Objects.equal(id, that.id) && Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }
}
