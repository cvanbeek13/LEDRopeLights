package com.CoryVanBeek.RopeLights.pi;

import com.CoryVanBeek.RopeLights.Color;
import com.CoryVanBeek.RopeLights.LightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This mode moves linearly between different colors.
 *
 * @author Cory Van Beek
 */
public class MeshLightsThread extends Thread{
    private static final Logger logger = LoggerFactory.getLogger(MeshLightsThread.class);
    private LightState state;
    private PiController controller;

    MeshLightsThread(LightState state, PiController controller) {
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
            Color next = state.nextColor();
            long cycleStart = System.currentTimeMillis();
            long time;
            while ((time = System.currentTimeMillis()) - cycleStart < state.getDeltaTime()) {
                double prog = progress(cycleStart, time, state.getDeltaTime());
                controller.setRed((int) Math.abs((curr.r * (1.0 - prog) - (next.r * prog)) / 2));
                controller.setGreen((int) Math.abs((curr.g * (1.0 - prog) - (next.g * prog)) / 2));
                controller.setBlue((int) Math.abs((curr.b * (1.0 - prog) - (next.b * prog)) / 2));
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            curr = next;
        }

        if(state.isFinished()) {
            logger.info("State finished");
            controller.notifyFinished();
        }
    }

    private double progress(long cycleStart, long time, long cycleDuration) {
        return Math.min(1.0, ((double) (time - cycleStart)) / (double) cycleDuration);
    }
}
