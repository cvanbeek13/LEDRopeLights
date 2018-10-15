package com.CoryVanBeek.RopeLights.thing;

import com.CoryVanBeek.RopeLights.LightsBridge;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

/**
 * Topic to delta accepted updates to the device shadow. This will happen when a new state is desired
 * and the application will update to the new desired state.
 *
 * @author Cory Van Beek
 */
public class TopicSubscriptionDelta extends AWSIotTopic {
    private static final String TOPIC = "$aws/things/StringLights/shadow/update/delta";
    private LightsBridge controller;

    public TopicSubscriptionDelta(LightsBridge controller) {
        super(TOPIC, AWSIotQos.QOS0);
        this.controller = controller;
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        String json = message.getStringPayload();
        System.out.println("Received delta update:" + json);
        controller.updateFromDeltaJSON(json);
    }
}
