package de.thedead2.progression_reloaded.client.data;

import de.thedead2.progression_reloaded.data.quest.QuestProgress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ClientQuests {

    private Set<ClientQuest> activeQuests;

    private Map<ClientQuest, QuestProgress> questProgress;


    public ClientQuests() {
        this.activeQuests = new HashSet<>();
        this.questProgress = new HashMap<>();
    }


    public void updateQuests(Set<ClientQuest> playerQuests, Map<ClientQuest, QuestProgress> questProgress) {
        this.activeQuests = playerQuests;
        this.questProgress = questProgress;

        this.updateStatus();
    }


    private void updateStatus() {

    }


    public Set<ClientQuest> getActiveQuests() {
        return this.activeQuests;
    }
}
