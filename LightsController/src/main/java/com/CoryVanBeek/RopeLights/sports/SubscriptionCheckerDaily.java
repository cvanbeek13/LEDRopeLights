package com.CoryVanBeek.RopeLights.sports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.*;
import java.util.Date;
import java.util.TimerTask;

/**
 * This TimerTask sends a request to the MySportsFeed API and schedules future requests
 * for a specific game
 *
 * @author Cory Van Beek
 */
public class SubscriptionCheckerDaily extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionCheckerGame.class);

    private Subscription subscription;
    private Date date;

    SubscriptionCheckerDaily(Subscription subscription, Date date) {
        this.subscription = subscription;
        this.date = date;
    }

    @Override
    public void run() {
        logger.info("Starting daily subscription check for team {}", subscription.getTeamId());
        SportsRequest request;
        if(Subscription.LEAGUE_BASEBALL.equalsIgnoreCase(subscription.getLeague()))
            request = new BaseballGamesRequest(subscription.getTeamId(), date);
        else if(Subscription.LEAGUE_HOCKEY.equalsIgnoreCase(subscription.getLeague()))
            request = new HockeyGamesRequest(subscription.getTeamId(), date);
        //TODO: Add other leagues
        else
            return;

        try {
            SportsResponse response = request.send();

            for(int i = 0; i < response.getGames().length; i++) {
                SportsGame game = response.getGames()[i];
                if(!game.isFinished()) {
                    Date endTime = game.estimatedEndTime();
                    logger.info("Team {} plays today.  Scheduling a check for {}", subscription.getTeamId(), endTime);
                    subscription.getTimer().schedule(new SubscriptionCheckerGame(subscription, date, game.getGameID()), endTime);
                }
            }
        } catch (IOException e) {
            logger.warn("IOException caught while requesting data for team " + subscription.getTeamId(), e);
        }

        //Schedule a check for tomorrow's games (12:01 a.m. tomorrow morning (UTC))
        logger.debug("Scheduling check for tomorrow morning for team: {}", subscription.getTeamId());
        LocalTime midnight = LocalTime.MIDNIGHT;
        Date tomorrow = Date.from(LocalDateTime.of(date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate(), midnight).plusDays(1).plusMinutes(1).toInstant(OffsetDateTime.now().getOffset()));
        if(tomorrow.before(new Date()))
            subscription.getTimer().schedule(new SubscriptionCheckerDaily(subscription, tomorrow), 2000);
        else
            subscription.getTimer().schedule(new SubscriptionCheckerDaily(subscription, tomorrow), tomorrow);
    }
}
