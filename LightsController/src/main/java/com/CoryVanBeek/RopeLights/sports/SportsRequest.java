package com.CoryVanBeek.RopeLights.sports;

import com.CoryVanBeek.RopeLights.thing.utils.LightsUtil;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Framework for requesting game updates from the MySportsFeed API for a specific team
 *
 * @author Cory Van Beek
 */
public abstract class SportsRequest<T extends SportsResponse> {
    private Class<T> tClass;
    static final DateFormat format = new SimpleDateFormat("yyyyMMdd");
    protected int teamId;
    protected String season;
    protected Date date;

    SportsRequest(Class<T> tClass) {
        this.tClass = tClass;
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
    public T send() {
        try {
            String apiToken = LightsUtil.getConfig("SportsToken");
            URL url = new URL (getURL());
            String encoding = new String(Base64.encodeBase64 ((apiToken + ":MYSPORTSFEEDS").getBytes()));

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + encoding);

            InputStream content = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line;
            StringBuilder all = new StringBuilder();
            while ((line = in.readLine()) != null) {
                all.append(line);
            }
            T response = tClass.newInstance();
            response.parse(all.toString());
            return response;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
