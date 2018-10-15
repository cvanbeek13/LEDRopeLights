package com.CoryVanBeek.RopeLights.pi;

import com.CoryVanBeek.RopeLights.LightState;
import com.CoryVanBeek.RopeLights.LightsBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This controller gets state requests from the main program and runs the proper LightsThread.
 * It also takes care of setting the r, g, and b color values for the lights.
 *
 * @author Cory Van Beek
 */
public class PiController {
    private static final Logger logger = LoggerFactory.getLogger(PiController.class);

    private static final int MAX_INTENSITY = 255;
    private static final int MAX_BRIGHTNESS = 8;
    private final int RED_PIN;
    private final int GREEN_PIN;
    private final int BLUE_PIN;

    private Thread lightsThread;
    private LightsBridge bridge;

    public PiController(LightsBridge bridge) {
        //Using the GPIO Library was causing the lights to flicker when in a pwm value less than the max.
        //However, command line calls were working using the pigpiod application, so we are just going to
        //make those calls on the runtime.
        RED_PIN = bridge.getProperties().getRed_pin();
        GREEN_PIN = bridge.getProperties().getGreen_pin();
        BLUE_PIN = bridge.getProperties().getBlue_pin();

        this.bridge = bridge;
        try {
            Runtime.getRuntime().exec("sudo pigpiod");
        } catch (IOException e) {
            logger.warn("Unable to start pigpiod");
        }
    }

    /**
     * Sets the current state on the controller
     *
     * @param state The Light State to execute
     */
    public synchronized void loadLightState(LightState state) {
        logger.info("Killing current Lights Thread");
        if(lightsThread != null && lightsThread.isAlive()) {
            lightsThread.interrupt();
            try {
                lightsThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!state.isOn()) {
            logger.debug("Turning off lights");
            setRed(0);
            setGreen(0);
            setBlue(0);
            return;
        }

        logger.debug("Setting Lights according to mode");
        switch (state.getMode()) {
            case Constant:
                lightsThread = new ConstantLightsThread(state, this);
                lightsThread.start();
                break;
            case Flash:
                lightsThread = new FlashLightsThread(state, this);
                lightsThread.start();
                break;
            case Sin:
                lightsThread = new SinLightsThread(state, this);
                lightsThread.start();
                break;
            case Mesh:
                lightsThread = new MeshLightsThread(state, this);
                lightsThread.start();
                break;
        }
    }

    void setRed(int intensity) {
        adjustIntensity(intensity, RED_PIN);
    }

    void setGreen(int intensity) {
        adjustIntensity(intensity, GREEN_PIN);
    }

    void setBlue(int intensity) {
        adjustIntensity(intensity, BLUE_PIN);
    }

    /**
     * Sets a pwm signal to the given pin
     * @param intensity The intensity of the signal from 0-255
     * @param pin The Raspberry Pi GPIO Pin to send the signal to.
     */
    private void adjustIntensity(int intensity, int pin) {
        if(intensity > MAX_INTENSITY)
            throw new IllegalArgumentException("Intensity must be less than " + MAX_INTENSITY);

        int brightness = bridge.getCurrentState().getBrightness();
        if(brightness < MAX_BRIGHTNESS)
            intensity = intensity * brightness / MAX_BRIGHTNESS;

        if(bridge.getProperties().isFlipped())
            intensity = MAX_INTENSITY - intensity;

        String e = "pigs p " + pin + " " + intensity;
        try {
            Runtime.getRuntime().exec(e);
        } catch (IOException e1) {
            logger.warn("Unable to set the pwm value for pin " + pin, e1);
        }
    }

    /**
     * Notifies the Lights Bridge that the current mode has completed.
     */
    public void notifyFinished() {
        Thread finishThread = new Thread(() -> bridge.finish());
        finishThread.start();
    }
}
