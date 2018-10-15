package com.CoryVanBeek.RopeLights.thing;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Topic to monitor rejected updates to the device shadow
 *
 * @author Cory Van Beek
 */
public class TopicSubscriptionRejected extends AWSIotTopic {
    private static final Logger logger = LoggerFactory.getLogger(TopicSubscriptionRejected.class);
    private static final String TOPIC = "$aws/things/StringLights/shadow/update/rejected";

    public TopicSubscriptionRejected() {
        super(TOPIC, AWSIotQos.QOS0);
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        logger.warn("Received message on rejected topic: {}. Error Code: {}. Payload: " + message.getStringPayload(), message.getErrorMessage(), message.getErrorCode().toString());
    }
}
