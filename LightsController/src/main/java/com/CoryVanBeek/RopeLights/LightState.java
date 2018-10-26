package com.CoryVanBeek.RopeLights;

import com.CoryVanBeek.RopeLights.thing.Holder;
import com.CoryVanBeek.RopeLights.thing.PropertiesHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An object used for holding a configuration for the lights to be in.
 *
 * @author Cory Van Beek
 */
public class LightState implements Serializable{
    private final boolean power;
    private final LightMode mode;
    private final int brightness;
    private final long deltaTime;
    private final long duration;
    private volatile long startTime;
    private volatile transient boolean started;
    private final List<Color> colors;
    private final int[] teams;
    private volatile transient int colorPointer;

    LightState(Holder structure, LightState current) {
        PropertiesHolder holder = structure.heldProperties();

        if(holder.power == null)
            power = current.isOn();
        else
            power = holder.power.equalsIgnoreCase("on");

        if(holder.mode == null)
            mode = current.getMode();
        else {
            switch (holder.mode.toLowerCase()) {
                case "flash":
                    mode = LightMode.Flash;
                    break;
                case "sin":
                    mode = LightMode.Sin;
                    break;
                case "mesh":
                    mode = LightMode.Mesh;
                    break;
                case "constant":
                default:
                    mode = LightMode.Constant;
                    break;
            }
        }

        if(holder.brightness == null || holder.brightness == 0)
            brightness = current.brightness;
        else {
            int temp = holder.brightness;
            if(temp > 8)
                temp = 8;
            else if(temp < 1)
                temp = 1;
            brightness = temp;
        }

        if(holder.deltaTime == null || holder.deltaTime == 0)
            deltaTime = current.deltaTime;
        else {
            long temp = holder.deltaTime;
            if(temp < 0)
                temp = -1L;
            deltaTime = temp;
        }

        if(holder.duration == null || holder.duration == 0)
            duration = current.duration;
        else {
            long temp = holder.duration;
            if(temp < 0)
                temp = -1L;
            duration = temp;
        }


        if(holder.colors == null)
            colors = current.colors;
        else {
            colors = new ArrayList<>();
            Collections.addAll(colors, holder.colors);
        }

        if(holder.teams == null)
            teams = current.teams;
        else {
            teams = holder.teams;
        }

    }

    LightState() {
        power = false;
        mode = LightMode.Constant;
        brightness = 8;
        deltaTime = 2000;
        duration = -1;
        colors = new ArrayList<>();
        colors.add(new Color(255 ,255, 255));
        teams = null;
        colorPointer = 0;
        startTime = System.currentTimeMillis();
    }

    public boolean isOn() {
        return power;
    }

    public LightMode getMode() {
        return mode;
    }

    public void start() {
        if(!started)
            startTime = System.currentTimeMillis();
        started = true;
    }

    public int getBrightness() {
        return brightness;
    }

    public long getDeltaTime() {
        return deltaTime;
    }

    public boolean isFinished() {
        return started && duration > 0 && startTime + duration < System.currentTimeMillis();
    }

    public long getDuration() {
        return duration;
    }

    public List<Color> getColors() {
        return colors;
    }

    public synchronized Color nextColor() {
        colorPointer++;
        if(colorPointer >= colors.size())
            colorPointer = 0;
        return colors.get(colorPointer);
    }

    public Color currColor() {
        return colors.get(colorPointer);
    }

    public boolean willExpire() {
        return duration != -1L;
    }

    public int[] getTeams() {
        return teams;
    }


    public enum LightMode {
        //TODO: Add Fade mode
        Constant, Flash, Sin, Mesh
    }

    @Override
    public String toString() {
        String result = "";
        result += "\tPower: " + (power ? "On" : "Off") + "\n";
        result += "\tMode: " + mode.toString() + "\n";
        result += "\tBrightness: " + brightness + "\n";
        result += "\tDuration: " + duration + "\n";
        result += "\tDelta Time: " + deltaTime + "\n";
        result += "\tColors: \n";
        for(Color c : colors) {
            result += "\t\tr: " + c.r + ", g: " + c.g + ", b: " + c.b + "\n";
        }
        result += "\tTeams:\n";
        if (teams != null) {
            for(int i: teams) {
                result += "\t\t" + i + "\n";
            }
        }
        return result;
    }
}
