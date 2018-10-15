package com.CoryVanBeek.RopeLights.sports;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class parses the specific MySportsFeed API JSON response for baseball games.
 * See their docs here: https://www.mysportsfeeds.com/data-feeds/api-docs/
 *
 * @author Cory Van Beek
 */
public class BaseballGamesResponse implements SportsResponse{
    private static final Logger logger = LoggerFactory.getLogger(BaseballGamesResponse.class);
    private static DateFormat dateFormat;


    private ObjectMapper mapper;
    public JSONGame[] games;

    public BaseballGamesResponse() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

    public static class JSONBaseballResponse {
        public JSONGame[] games;
    }

    public static class JSONGame implements SportsGame {
        static final String COMPLETED = "COMPLETED";
        static final String LIVE = "LIVE";
        static final String TOP_INNING = "TOP";

        static final int HALF_INNING_SECONDS = 180;
        static final int INTERMISSION_SECONDS = 120;

        public JSONSchedule schedule;
        public JSONScore score;

        public JSONGame() {

        }

        @Override
        public boolean isFinished() {
            return schedule != null && COMPLETED.equals(schedule.playedStatus);
        }

        @Override
        public int winnerTeamId() {
            if(!isFinished()) return -1;
            if(score.awayScoreTotal > score.homeScoreTotal)
                return schedule.awayTeam.id;
            else if(score.homeScoreTotal > score.awayScoreTotal)
                return schedule.homeTeam.id;
            else
                return -2;
        }

        @Override
        public Date estimatedEndTime() {
            if(isFinished())
                return new Date();
            Date end = Date.from(Instant.now().plusSeconds(120));

            int fullInningsRemaining = 9;
            if(schedule != null) {
                Date start = new Date();
                try {
                    if(LIVE.equalsIgnoreCase(schedule.playedStatus)) {
                        fullInningsRemaining = 9 - score.currentInning;
                    }
                    else
                        start = dateFormat.parse(schedule.startTime);
                } catch (ParseException e) {
                    logger.debug("Unable to parse start time", e);
                    start = new Date();
                }
                ZonedDateTime zonedStartTime = start.toInstant().atZone(ZoneId.of("UTC"));
                zonedStartTime = zonedStartTime.plusSeconds(fullInningsRemaining * 2 * (HALF_INNING_SECONDS + INTERMISSION_SECONDS) - INTERMISSION_SECONDS);
                if(TOP_INNING.equalsIgnoreCase(score.currentInningHalf))
                    zonedStartTime = zonedStartTime.plusSeconds(HALF_INNING_SECONDS + INTERMISSION_SECONDS);
                Date newEnd = Date.from(zonedStartTime.toInstant());

                if(newEnd.after(end))
                    end = newEnd;
            }
            return end;
        }
    }

    public static class JSONSchedule {
        public String startTime;
        public JSONTeam homeTeam;
        public JSONTeam awayTeam;
        public String playedStatus;

    }

    public static class JSONTeam {
        public int id;
    }

    public static class JSONScore {
        public int awayScoreTotal;
        public int homeScoreTotal;
        public int currentInning;
        public String currentInningHalf;
    }
}
