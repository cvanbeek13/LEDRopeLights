package com.CoryVanBeek.RopeLights.thing;

import com.CoryVanBeek.RopeLights.Color;
import com.CoryVanBeek.RopeLights.LightState;

/**
 * JSON object for sending the desired and reported state to the IOT Device shadow after a LightState is finished
 *
 * @author Cory Van Beek
 */
public class JSONStructureSentBoth {

    public JSONStructureSentBoth(LightState s) {
        PropertiesHolder r = state.reported;
        r.power = s.isOn() ? "on" : "off";
        r.mode = s.getMode().name().toLowerCase();
        r.brightness = s.getBrightness();
        r.duration = s.getDuration();
        r.deltaTime = s.getDeltaTime();
        r.colors = new Color[s.getColors().size()];
        for(int i = 0; i < r.colors.length; i++){
            r.colors[i] = s.getColors().get(i);
        }
        r.teams = s.getTeams();
        state.desired = state.reported;
    }

    public State state = new State();

    public class State {
        public PropertiesHolder reported = new PropertiesHolder();
        public PropertiesHolder desired = new PropertiesHolder();
    }
}
