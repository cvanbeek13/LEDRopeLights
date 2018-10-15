package com.CoryVanBeek.RopeLights.pi;

import com.CoryVanBeek.RopeLights.Color;
import com.CoryVanBeek.RopeLights.LightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This thread switches between the colors with no transition.
 *
 * @author Cory Van Beek
 */
public class FlashLightsThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(FlashLightsThread.class);
    private LightState state;
    private PiController controller;

    FlashLightsThread(LightState state, PiController controller) {
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
        while (!state.isFinished()) {
            controller.setRed(curr.r);
            controller.setGreen(curr.g);
            controller.setBlue(curr.b);
            try {
                Thread.sleep(state.getDeltaTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            curr = state.nextColor();
        }

        if(state.isFinished()) {
            logger.info("State finished");
            controller.notifyFinished();
        }
    }
}
