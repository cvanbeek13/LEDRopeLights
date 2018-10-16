package com.CoryVanBeek.RopeLights.sports;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * This class parses the specific MySportsFeed API JSON response for baseball games.
 * See their docs here: https://www.mysportsfeeds.com/data-feeds/api-docs/
 *
 * @author Cory Van Beek
 */
public class BaseballGamesResponse implements SportsResponse {
    private static final Logger logger = LoggerFactory.getLogger(BaseballGamesResponse.class);


    private ObjectMapper mapper;
    public BaseballGame[] games;

    public BaseballGamesResponse() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void parse(String jsonString) {
        try {
            JSONBaseballResponse response = mapper.readValue(jsonString, JSONBaseballResponse.class);
            this.games = response.games;
        } catch (IOException e) {
            logger.error("IOException while reading response", e);
        }
    }

    @Override
    public SportsGame[] getGames() {
        return games;
    }

    @Override
    public SportsGame getGame(int id) {
        for (BaseballGame game : games) {
            if (game.schedule != null && game.schedule.id == id)
                return game;
        }
        return null;
    }

    public static class JSONBaseballResponse {
        public BaseballGame[] games;
    }

}