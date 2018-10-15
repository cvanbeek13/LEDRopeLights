package com.CoryVanBeek.RopeLights.thing;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object used to send messages to the Iot Shadow
 *
 * @author Cory Van Beek
 */
public class UpdateShadowMessage extends AWSIotMessage {
    private static final Logger logger = LoggerFactory.getLogger(UpdateShadowMessage.class);
    private static final String TOPIC = "$aws/things/StringLights/shadow/update";
    private String payload;

    public UpdateShadowMessage(String payload) {
        super(TOPIC, AWSIotQos.QOS0, payload);
        this.payload = payload;
    }

    @Override
    public void onSuccess() {
        // called when message publishing succeeded
    }

    @Override
    public void onFailure() {
        logger.warn("Failed to push message: {}:\n{}", payload, this.errorMessage);
    }

    @Override
    public void onTimeout() {
        logger.warn("Timeout when pushing message: {}:\n{}", payload, this.errorMessage);
    }
}