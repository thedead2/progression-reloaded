package de.thedead2.progression_reloaded.client.data;

import de.thedead2.progression_reloaded.client.display.TeamDisplayInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;


@OnlyIn(Dist.CLIENT)
public class ClientPlayer {

    private final ClientQuests quests;

    private final ClientLevels levels;

    private int extraLives;

    private TeamDisplayInfo team;


    public ClientPlayer() {
        this.quests = new ClientQuests();
        this.levels = new ClientLevels();
    }


    public ClientQuests getQuests() {
        return quests;
    }


    public int getExtraLives() {
        return extraLives;
    }


    public Optional<TeamDisplayInfo> getTeam() {
        return Optional.ofNullable(team);
    }


    public void onSync(int extraLives, TeamDisplayInfo team) {
        this.extraLives = extraLives;
        this.team = team;
    }


    public void updateExtraLives(int extraLives) {
        this.extraLives = extraLives;
    }


    public void updateTeam(TeamDisplayInfo team) {
        this.team = team;
    }


    public ClientLevels getLevels() {
        return this.levels;
    }
}
