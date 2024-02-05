package de.thedead2.progression_reloaded.api;

import net.minecraft.network.chat.Component;


public interface IStatusChecker {

    void reset();

    boolean updateAndCheck();

    boolean check();

    Component getStatus();
}
