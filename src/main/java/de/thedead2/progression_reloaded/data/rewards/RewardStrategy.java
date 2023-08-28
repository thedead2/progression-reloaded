package de.thedead2.progression_reloaded.data.rewards;

import de.thedead2.progression_reloaded.player.types.PlayerTeam;
import de.thedead2.progression_reloaded.player.types.SinglePlayer;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public enum RewardStrategy {
    ALL {
        @Override
        public void reward(Set<IReward> questRewards, PlayerTeam team) {
            team.getActiveMembers().forEach(player -> questRewards.forEach(reward -> reward.rewardPlayer(player.getServerPlayer())));
        }
    },
    RANDOM {
        @Override
        public void reward(Set<IReward> questRewards, PlayerTeam team) {
            var teamMembers = team.getActiveMembers();
            Random rand = new Random();
            int index = rand.nextInt(teamMembers.size());
            Iterator<SinglePlayer> iterator = teamMembers.iterator();
            for(int i = 0; i < index; i++) {
                iterator.next();
            }
            SinglePlayer player = iterator.next();
            questRewards.forEach(reward -> reward.rewardPlayer(player.getServerPlayer()));
        }
    };


    public void reward(Set<IReward> questRewards, SinglePlayer player) {
        if(player.isInTeam()) {
            this.reward(questRewards, player.getTeam().orElseThrow());
        }
        else {
            questRewards.forEach(reward -> reward.rewardPlayer(player.getServerPlayer()));
        }
    }


    public abstract void reward(Set<IReward> questRewards, PlayerTeam team);
}
