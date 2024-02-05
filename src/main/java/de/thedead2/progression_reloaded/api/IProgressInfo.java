package de.thedead2.progression_reloaded.api;

import de.thedead2.progression_reloaded.api.network.INetworkSerializable;


public interface IProgressInfo<T extends IProgressable<T>> extends INetworkSerializable, INbtSerializable {

    /**
     * @return the progress of this {@link IProgressInfo} in stopPercent
     **/
    float getPercent();

    /**
     * @return true if the progress of this {@link IProgressInfo} is complete
     **/
    boolean isDone();

    /**
     * Resets the progress of this {@link IProgressInfo}
     **/
    void reset();

    /**
     * Causes the progress of this {@link IProgressInfo} to be completed
     **/
    void complete();

    T getProgressable();
}
