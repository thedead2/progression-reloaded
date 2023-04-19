package de.thedead2.progression_reloaded.player;

import de.thedead2.progression_reloaded.helpers.AchievementHelper;
import de.thedead2.progression_reloaded.helpers.MCClientHelper;
import de.thedead2.progression_reloaded.helpers.PlayerHelper;
import de.thedead2.progression_reloaded.player.data.AbilityStats;
import de.thedead2.progression_reloaded.player.data.CustomStats;
import de.thedead2.progression_reloaded.player.data.Points;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.UUID;

import static de.thedead2.progression_reloaded.gui.core.GuiList.GROUP_EDITOR;

public class PlayerDataClient extends PlayerDataCommon {
    private static PlayerDataClient INSTANCE = new PlayerDataClient();

    public static PlayerDataClient getInstance() {
        return INSTANCE;
    }

    public UUID getUUID() {
        return PlayerHelper.getUUIDForPlayer(MCClientHelper.getPlayer());
    }

    public void setAbilities(AbilityStats abilities) {
        this.abilities = abilities;
    }

    public void setCustomData(CustomStats data) {
        this.custom = data;
    }

    public void setPoints(Points points) {
        this.points = points;
    }

    @Override
    public void setTeam(PlayerTeam team) {
        if (this.team != null && this.team.getOwner() != team.getOwner() && this.team != team) {
            AchievementHelper.display(new ItemStack(Items.BANNER), "Joined " + PlayerTracker.getClientPlayer().getTeam().getName());
        }

        super.setTeam(team);
        GROUP_EDITOR.clear();
    }
}
