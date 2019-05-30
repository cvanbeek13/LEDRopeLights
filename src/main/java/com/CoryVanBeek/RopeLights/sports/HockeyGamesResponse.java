package com.CoryVanBeek.RopeLights.sports;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class parses the specific MySportsFeed API JSON response for hockey games.
 * See their docs here: https://www.mysportsfeeds.com/data-feeds/api-docs/
 *
 * @author Cory Van Beek
 */
public class HockeyGamesResponse implements SportsResponse {
    private static final Logger logger = LoggerFactory.getLogger(HockeyGamesResponse.class);


    private ObjectMapper mapper;
    public HockeyGame[] games;

    public HockeyGamesResponse() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void parse(String jsonString) {
        try {
            logger.trace("Parsing hockey response: \n{}", jsonString);
            JSONHockeyResponse response = mapper.readValue(jsonString, JSONHockeyResponse.class);
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
        for (HockeyGame game : games) {
            if (game.schedule != null && game.schedule.id == id) {
                return game;
            }

        }
        return null;
    }

    public static class JSONHockeyResponse {
        public HockeyGame[] games;
    }

}