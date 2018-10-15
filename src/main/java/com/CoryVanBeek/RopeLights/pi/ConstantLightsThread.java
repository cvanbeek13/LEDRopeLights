package com.CoryVanBeek.RopeLights.pi;

import com.CoryVanBeek.RopeLights.Color;
import com.CoryVanBeek.RopeLights.LightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread is used to turn on the lights to a single color
 *
 * @author Cory Van Beek
 */
public class ConstantLightsThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ConstantLightsThread.class);
    private LightState state;
    private PiController controller;

    ConstantLightsThread(LightState state, PiController controller) {
        super();
        this.state = state;
        this.controller = controller;
    }

    @Override
    public void run() {
        Color c = state.getColors().get(0);
        controller.setRed(c.r);
        controller.setGreen(c.g);
        controller.setBlue(c.b);
        while (!Thread.currentThread().isInterrupted() && !state.isFinished()) {
            try {
                //Todo: Make this more efficient and get rid of the polling
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.debug("Thread Interrupted");
                Thread.currentThread().interrupt();
            }
        }


        if(state.isFinished()) {
            logger.info("State finished");
            controller.notifyFinished();
        }
    }
}
