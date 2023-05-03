package de.thedead2.progression_reloaded.data.rewards;

import de.thedead2.progression_reloaded.player.PlayerTeam;
import de.thedead2.progression_reloaded.player.SinglePlayer;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public enum RewardStrategy {
    ALL,
    RANDOM;

    public void reward(Set<IReward> questRewards, PlayerTeam team) {
        var teamMembers = team.getActiveMembers();
        switch (this){
            case ALL -> teamMembers.forEach(player -> questRewards.forEach(reward -> reward.rewardPlayer(player.getPlayer())));
            case RANDOM -> {
                Random rand = new Random();
                int index = rand.nextInt(teamMembers.size());
                Iterator<SinglePlayer> iterator = teamMembers.iterator();
                for (int i = 0; i < index; i++) {
                    iterator.next();
                }
                SinglePlayer player = iterator.next();
                questRewards.forEach(reward -> reward.rewardPlayer(player.getPlayer()));
            }
        }
    }

    public void reward(Set<IReward> questRewards, SinglePlayer player) {
        if(player.isInTeam()){
            this.reward(questRewards, player.getTeam().orElseThrow());
        }
        else {
            questRewards.forEach(reward -> reward.rewardPlayer(player.getPlayer()));
        }
    }
}
