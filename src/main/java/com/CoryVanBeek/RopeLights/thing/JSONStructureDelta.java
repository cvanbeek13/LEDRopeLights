package com.CoryVanBeek.RopeLights.thing;

/**
 * JSON object received when the desired state of the IOT Device shadow is different than the reported.
 * This is used to update the device to the desired state.
 *
 * @author Cory Van Beek
 */
public class JSONStructureDelta implements Holder {
    public PropertiesHolder state = new PropertiesHolder();

    @Override
    public PropertiesHolder getProps() {
        return state;
    }
}
