package com.CoryVanBeek.RopeLights.thing;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Topic to monitor accepted updates to the device shadow
 *
 * @author Cory Van Beek
 */
public class TopicSubscriptionAccepted extends AWSIotTopic {
    private static final Logger logger = LoggerFactory.getLogger(TopicSubscriptionAccepted.class);
    private static final String TOPIC = "$aws/things/StringLights/shadow/update/accepted";

    public TopicSubscriptionAccepted() {
        super(TOPIC, AWSIotQos.QOS0);
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        logger.debug("Received message on accepted topic: {}", message.getStringPayload());
    }
}
