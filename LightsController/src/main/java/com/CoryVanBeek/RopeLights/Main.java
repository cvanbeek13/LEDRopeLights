package com.CoryVanBeek.RopeLights;

import com.CoryVanBeek.RopeLights.thing.TopicSubscriptionAccepted;
import com.CoryVanBeek.RopeLights.thing.TopicSubscriptionDelta;
import com.CoryVanBeek.RopeLights.thing.TopicSubscriptionRejected;
import com.CoryVanBeek.RopeLights.thing.utils.CommandArguments;
import com.CoryVanBeek.RopeLights.thing.utils.LightsUtil;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Main class for initializing the application
 *
 * @author Cory Van Beek
 */
public class Main implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);


    private static AWSIotMqttClient awsIotClient;
    private CommandArguments arguments;

    private Main(CommandArguments arguments) {
        this.arguments = arguments;
    }

    private static void initClient(CommandArguments arguments) {
        String clientEndpoint = arguments.getNotNull("clientEndpoint", LightsUtil.getConfig("clientEndpoint"));
        String clientId = arguments.getNotNull("clientId", LightsUtil.getConfig("clientId"));

        String certificateFile = arguments.get("certificateFile", LightsUtil.getConfig("certificateFile"));
        String privateKeyFile = arguments.get("privateKeyFile", LightsUtil.getConfig("privateKeyFile"));
        if (awsIotClient == null && certificateFile != null && privateKeyFile != null) {
            String algorithm = arguments.get("keyAlgorithm", LightsUtil.getConfig("keyAlgorithm"));
            LightsUtil.KeyStorePasswordPair pair = LightsUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);

            if (pair != null) {
                awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
            }
        }

        if (awsIotClient == null) {
            String awsAccessKeyId = arguments.get("awsAccessKeyId", LightsUtil.getConfig("awsAccessKeyId"));
            String awsSecretAccessKey = arguments.get("awsSecretAccessKey", LightsUtil.getConfig("awsSecretAccessKey"));
            String sessionToken = arguments.get("sessionToken", LightsUtil.getConfig("sessionToken"));

            if (awsAccessKeyId != null && awsSecretAccessKey != null) {
                awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey,
                        sessionToken);
            }
        }

        if (awsIotClient == null) {
            throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
        }
    }

    public static void main(String[] args) {
        Main main = new Main(CommandArguments.parse(args));
        main.run();
    }

    public void run() {
        try {
            LightsProperties properties;
            if(arguments.hasArgument("f"))
                properties = new LightsProperties(new File(arguments.getNotNull("f")));
            else
                properties = new LightsProperties();

            initClient(arguments);

            String thingName = arguments.getNotNull("thingName", LightsUtil.getConfig("thingName"));
            AWSIotDevice device = new AWSIotDevice(thingName);

            awsIotClient.attach(device);
            awsIotClient.setMaxConnectionRetries(0);
            awsIotClient.connect();

            LightsBridge controller = new LightsBridge(awsIotClient, device, properties);

            awsIotClient.subscribe(new TopicSubscriptionDelta(controller));
            awsIotClient.subscribe(new TopicSubscriptionRejected());
            awsIotClient.subscribe(new TopicSubscriptionAccepted());

            InputStream keystoreStream = Main.class.getResourceAsStream("sportsKeyStore");
            char[] keystorePassword = "password".toCharArray();
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(keystoreStream, keystorePassword);
                TrustManagerFactory trustFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustFactory.init(keyStore);
                TrustManager[] trustManagers = trustFactory.getTrustManagers();

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustManagers, null);
                SSLContext.setDefault(sslContext);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
                logger.warn("Error setting up MySportsFeed Certificate", e);
            }

        } catch (AWSIotException e) {
            e.printStackTrace();
        }
    }
}
