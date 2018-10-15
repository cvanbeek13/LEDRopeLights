package com.CoryVanBeek.RopeLights.thing;

/**
 * JSON object for getting the initial reported state from the IOT Device shadow
 *
 * @author Cory Van Beek
 */
public class JSONStructureReceived implements Holder {
    public State state = new State();

    @Override
    public PropertiesHolder getProps() {
        return state.reported;
    }

    public class State {
        public PropertiesHolder reported = new PropertiesHolder();
    }
}
