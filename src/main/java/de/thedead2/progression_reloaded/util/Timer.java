package de.thedead2.progression_reloaded.util;

import org.apache.commons.lang3.time.StopWatch;


public class Timer extends StopWatch {

    public Timer() {
        this.start();
    }


    @Override
    public void start() {
        if(this.isStarted()) {
            this.stop();
            super.start();
        }
        else {
            if(this.isStopped()) {
                this.reset();
            }
            super.start();
        }
    }


    public void stop() {
        super.stop();
        this.reset();
    }
}
