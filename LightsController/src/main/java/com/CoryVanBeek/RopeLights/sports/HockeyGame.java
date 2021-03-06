package com.CoryVanBeek.RopeLights.sports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class represents the JSON structure returned by MySportsFeed for Hockey games.
 * It also computes info about the game based on the parsed data from the API.
 *
 * @author Cory Van Beek
 */
public class HockeyGame implements SportsGame {
    private static final Logger logger = LoggerFactory.getLogger(HockeyGame.class);

    private static final int PERIOD_SECONDS = 60 * 20;
    private static final int INTERMISSION_SECONDS = 60 * 15 + 30;

    private DateFormat dateFormat;

    public JSONSchedule schedule;
    public JSONScore score;

    public HockeyGame() {
        dateFormat = new SimpleDateFormat(RESPONSE_DATE_PATTERN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public int getGameID() {
        if(schedule == null)
            return -1;
        else
            return schedule.id;
    }

    @Override
    public boolean isFinished() {
        return schedule != null && (COMPLETED.equals(schedule.playedStatus) || COMPLETED_PENDING_REVIEW.equals(schedule.playedStatus) );
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

        int fullPeriodsRemaining = 3;
        int intermissionsRemaining = 2;
        int periodTimeRemaining = 0;
        if(schedule != null) {
            Date start = new Date();
            try {
                if(LIVE.equalsIgnoreCase(schedule.playedStatus)) {
                    if(score.currentPeriod != null) {
                        fullPeriodsRemaining = 3 - score.currentPeriod;
                        intermissionsRemaining = 3 - score.currentPeriod;
                        periodTimeRemaining = score.currentPeriodSecondsRemaining;
                    }
                    else if(score.currentIntermission != null) {
                        fullPeriodsRemaining = 3 - score.currentIntermission;
                        intermissionsRemaining = 2 - score.currentIntermission;
                    }
                }
                else
                    start = dateFormat.parse(schedule.startTime);
            } catch (ParseException e) {
                logger.debug("Unable to parse start time", e);
                start = new Date();
            }
            ZonedDateTime zonedStartTime = start.toInstant().atZone(ZoneId.of("UTC"));
            zonedStartTime = zonedStartTime.plusSeconds(fullPeriodsRemaining * PERIOD_SECONDS + intermissionsRemaining * INTERMISSION_SECONDS + periodTimeRemaining);
            Date newEnd = Date.from(zonedStartTime.toInstant());

            if(newEnd.after(end))
                end = newEnd;
        }
        return end;
    }

    public static class JSONSchedule {
        public int id;
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
        public Integer currentPeriod;
        public Integer currentIntermission;
        public Integer currentPeriodSecondsRemaining;
    }
}