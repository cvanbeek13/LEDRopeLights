package com.CoryVanBeek.RopeLights.pi;

import com.CoryVanBeek.RopeLights.Color;
import com.CoryVanBeek.RopeLights.LightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This mode uses a sin wave to increase the intensity of the color, and then fade it back out.
 * It moves to the next color after one wave cycle is complete.
 *
 * @author Cory Van Beek
 */
public class SinLightsThread extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(SinLightsThread.class);
    private LightState state;
    private PiController controller;

    SinLightsThread(LightState state, PiController controller) {
        super();
        this.state = state;
        this.controller = controller;
    }

    @Override
    public void run() {
        controller.setRed(0);
        controller.setGreen(0);
        controller.setBlue(0);
        Color curr = state.currColor();
        while (!Thread.currentThread().isInterrupted() && !state.isFinished()) {
            long cycleStart = System.currentTimeMillis();
            int midRed = curr.r / 2;
            int midGreen = curr.g / 2;
            int midBlue = curr.b / 2;
            long time;
            while ((time = System.currentTimeMillis()) - cycleStart < state.getDeltaTime()) {
                double prog = progress(cycleStart, time, state.getDeltaTime());
                double cosValue = Math.cos(2 * prog * Math.PI);
                controller.setRed(  (int) (-1 * midRed * cosValue + midRed));
                controller.setGreen((int) (-1 * midGreen * cosValue + midGreen));
                controller.setBlue( (int) (-1 * midBlue * cosValue + midBlue));
            }
            curr = state.nextColor();
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if(state.isFinished()) {
            logger.info("State finished");
            controller.notifyFinished();
        }
    }

    /**
     * Gets the progress through the current wave cycle on a scale from 0 to 1.
     *
     * @param cycleStart The start time of the wave cycle
     * @param time The current time
     * @param cycleDuration The length of the wave cycle
     * @return The progress through the current wave
     */
    private double progress(long cycleStart, long time, long cycleDuration) {
        return Math.min(1.0, ((double) (time - cycleStart)) / (double) cycleDuration);
    }
}
