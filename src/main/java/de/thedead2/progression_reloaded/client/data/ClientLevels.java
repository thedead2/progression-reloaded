package de.thedead2.progression_reloaded.client.data;

import java.util.HashMap;
import java.util.Map;


public class ClientLevels {

    private ClientLevel currentLevel;

    private Map<ClientLevel, ClientLevelProgress> levelProgress;


    public ClientLevels() {
        this.currentLevel = null;
        this.levelProgress = new HashMap<>();
    }


    public void updateLevels(ClientLevel currentLevel, Map<ClientLevel, ClientLevelProgress> levelProgress) {
        this.currentLevel = currentLevel;
        this.levelProgress = levelProgress;

        this.updateStatus();
    }


    private void updateStatus() {

    }


    public ClientLevel getCurrent() {
        return currentLevel;
    }


    public void updateCurrentLevel(ClientLevel level) {
        this.currentLevel = level;
    }
}
