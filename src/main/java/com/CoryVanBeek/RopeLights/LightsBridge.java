package com.CoryVanBeek.RopeLights;

import com.CoryVanBeek.RopeLights.pi.PiController;
import com.CoryVanBeek.RopeLights.sports.SubscriptionManager;
import com.CoryVanBeek.RopeLights.thing.*;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Stack;

/**
 * This class acts as the main bridge between the RaspberryPi and the Iot Device shadow.
 * It processes incoming desired state requests, sends them to the PiController and then updates
 * the device shadow with the current state.  It also probes the Subscription Manager to subscribe
 * and unsubscribe when an update is received.
 *
 * @author Cory Van Beek
 */
public class LightsBridge {
    private static final Logger logger = LoggerFactory.getLogger(LightsBridge.class);
    private Stack<LightState> states;
    private ObjectMapper objectMapper;
    private AWSIotDevice device;
    private AWSIotMqttClient client;
    private PiController controller;
    private LightsProperties properties;
    private SubscriptionManager subscriptionManager;

    public LightsBridge(AWSIotMqttClient client , AWSIotDevice device, LightsProperties properties) {
        this.client = client;
        this.device = device;
        this.properties = properties;
        this.subscriptionManager = new SubscriptionManager(this, properties);
        states = new Stack<>();
        states.push(new LightState());
        controller = new PiController(this);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                                                                 final JavaType type,
                                                                 BeanDescription beanDesc,
                                                                 final JsonDeserializer<?> deserializer) {
                return new JsonDeserializer<Enum>() {
                    @Override
                    public Enum deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                        Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
                        return Enum.valueOf(rawClass, jp.getValueAsString().toUpperCase());
                    }
                };
            }
        });
        module.addSerializer(Enum.class, new StdSerializer<Enum>(Enum.class) {
            @Override
            public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeString(value.name().toLowerCase());
            }
        });
        objectMapper.registerModule(module);

        updateInitialFromShadow();
    }

    private void updateInitialFromShadow() {
        try {
            String shadowState = device.get();
            logger.info("Updating Lights Controller");
            JSONStructureReceived structure = objectMapper.readValue(shadowState, JSONStructureReceived.class);
            LightState reportedState = new LightState(structure, states.peek());
            setCurrentState(reportedState);
            //updateFromDeltaJSON(shadowState);

        } catch (AWSIotException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the lights from an Iot delta message
     *
     * @param jsonString The json string from the message
     */
    public void updateFromDeltaJSON(String jsonString) {
        Thread updateThread = new Thread(() -> {
            try {
                logger.info("Updating Lights Controller");
                JSONStructureDelta structure = objectMapper.readValue(jsonString, JSONStructureDelta.class);
                LightState desiredState = new LightState(structure, states.peek());
                setCurrentState(desiredState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        updateThread.start();
    }

    /**
     * Sets the current light state, overwriting the current state.
     *
     * @param state The LightState to apply
     */
    public synchronized void setCurrentState(LightState state) {
        if(state.willExpire())
            states.push(state);
        else {
            popToRoot();
            states.push(state);
        }
        startCurrentState();
    }

    /**
     * Sends the properties as a desired request to the Iot Shadow.  This is used in the subscriptions.
     * When a subscription's event is triggered, it sends a desired response to the shadow, which in
     * turn sends a delta update which the application receives and updates the lights.
     *
     * @param holder The lights properties holder to request
     */
    public synchronized void sendDesired(PropertiesHolder holder) {
        JSONStructureDesired desired = new JSONStructureDesired(holder);
        sendObjectToShadow(desired);
    }

    private void popToRoot() {
        while (states.size() > 1) {
            states.pop();
        }
    }

    private void startCurrentState(boolean shouldUpdateShadow) {
        if(shouldUpdateShadow)
            updateShadow(getCurrentState());

        controller.loadLightState(getCurrentState());

        getCurrentState().start();
        subscriptionManager.manage(getCurrentState().getTeams());
        logger.info("State updated: \n" + getCurrentState().toString());
    }

    private void startCurrentState() {
        startCurrentState(true);
    }

    private void updateShadow(LightState state) {
        JSONStructureSentReported structureSent = new JSONStructureSentReported(state);
        sendObjectToShadow(structureSent);
    }

    private void sendObjectToShadow(Object send) {
        try {
            String jsonString = objectMapper.writeValueAsString(send);
            logger.debug("Sending Response: {}", jsonString);
            UpdateShadowMessage msg = new UpdateShadowMessage(jsonString);
            client.publish(msg, 2000);
        } catch (JsonProcessingException | AWSIotException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is invoked by the PiController when a state is finished.  This will cause
     * the next state on the Stack to be started.
     */
    public void finish() {
        states.pop();
        if(states.size() == 0)
            states.push(new LightState());

        updateShadowDesiredAndReported(getCurrentState());
        startCurrentState(false);
    }

    private void updateShadowDesiredAndReported(LightState currentState) {
        JSONStructureSentBoth structureSentDesired = new JSONStructureSentBoth(currentState);
        sendObjectToShadow(structureSentDesired);
    }

    public LightsProperties getProperties() {
        return properties;
    }

    /**
     * Gets the current state of the lights
     *
     * @return The current lights state
     */
    public LightState getCurrentState() {
        return states.peek();
    }
}
