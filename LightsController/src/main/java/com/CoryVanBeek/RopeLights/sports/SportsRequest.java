package com.CoryVanBeek.RopeLights.sports;

import com.CoryVanBeek.RopeLights.thing.utils.LightsUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Framework for requesting game updates from the MySportsFeed API for a specific team
 *
 * @author Cory Van Beek
 */
public abstract class SportsRequest<T extends SportsResponse> {
    private static final Logger logger = LoggerFactory.getLogger(SportsRequest.class);

    private Class<T> tClass;
    final DateFormat format;
    protected int teamId;
    protected String season;
    protected Date date;

    SportsRequest(Class<T> tClass) {
        this.tClass = tClass;
        format =  new SimpleDateFormat("yyyyMMdd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Gets the URL for a sports request
     *
     * @return The URL for the specific sport
     */
    abstract String getURL();

    /**
     * Sends the request to the MySportsFeed API
     *
     * @return The SportsResponse for the correct sport (T)
     */
    public T send() throws IOException {
        String apiToken = LightsUtil.getConfig("SportsToken");
        URL url = new URL (getURL());
        String encoding = new String(Base64.encodeBase64 ((apiToken + ":MYSPORTSFEEDS").getBytes()));

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + encoding);
        logger.trace("Sending request to {}", url);

        InputStream content = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(content));
        String line;
        StringBuilder all = new StringBuilder();
        while ((line = in.readLine()) != null) {
            all.append(line);
        }

        T response = null;
        try {
            response = tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        response.parse(all.toString());
        return response;
    }
}
