package com.CoryVanBeek.RopeLights.thing;

/**
 * JSON object for sending the desired state to the IOT Device shadow
 *
 * @author Cory Van Beek
 */
public class JSONStructureDesired implements Holder {
    public State state = new State();

    public JSONStructureDesired(PropertiesHolder holder) {
        state.desired = holder;
    }

    @Override
    public PropertiesHolder heldProperties() {
        return state.desired;
    }

    public class State {
        public PropertiesHolder desired = new PropertiesHolder();
    }
}
