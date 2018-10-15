package com.CoryVanBeek.RopeLights;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class holds default application policies that can be overwritten by passing in
 * a configuration file as a command line argument using -f <file_name></file_name>
 *
 * Properties in the file will have the following syntax:
 * key=value
 *
 * @author Cory Van Beek
 */
public class LightsProperties {
    private static final Logger logger = LoggerFactory.getLogger(LightsBridge.class);
    public static final boolean DEFAULT_FLIPPED = false;
    public static final int DEFAULT_RED_PIN = 17;
    public static final int DEFAULT_GREEN_PIN = 22;
    public static final int DEFAULT_BLUE_PIN = 24;

    private boolean flipped;
    private int red_pin;
    private int green_pin;
    private int blue_pin;

    public LightsProperties() {
        this.flipped = DEFAULT_FLIPPED;
        this.red_pin = DEFAULT_RED_PIN;
        this.green_pin = DEFAULT_GREEN_PIN;
        this.blue_pin = DEFAULT_BLUE_PIN;
    }

    public LightsProperties(File file) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(file));

            this.flipped = Boolean.parseBoolean(props.getProperty("flipped", "" + DEFAULT_FLIPPED));
            this.red_pin = Integer.parseInt(props.getProperty("red_pin", "" + DEFAULT_RED_PIN));
            this.green_pin = Integer.parseInt(props.getProperty("green_pin", "" + DEFAULT_GREEN_PIN));
            this.blue_pin = Integer.parseInt(props.getProperty("blue_pin", "" + DEFAULT_BLUE_PIN));
        } catch (IOException e) {
            logger.warn("Unable to find properties file at {}", file.getAbsolutePath());
        }
    }

    public boolean isFlipped() { return flipped; }

    public int getRed_pin() {
        return red_pin;
    }

    public int getGreen_pin() {
        return green_pin;
    }

    public int getBlue_pin() {
        return blue_pin;
    }
}
