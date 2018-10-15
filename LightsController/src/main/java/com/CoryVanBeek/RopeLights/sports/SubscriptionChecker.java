package com.CoryVanBeek.RopeLights.sports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.TimerTask;

/**
 * This TimerTask sends a request to the MySportsFeed API and schedules future requests
 *
 * @author Cory Van Beek
 */
public class SubscriptionChecker extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionChecker.class);

    private Subscription subscription;

    SubscriptionChecker(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void run() {
        logger.debug("Starting subscription check for team {}", subscription.getTeamId());
        SportsRequest request;
        if(Subscription.LEAGUE_BASEBALL.equalsIgnoreCase(subscription.getLeague()))
            request = new BaseballGamesRequest(subscription.getTeamId());
        //TODO: Add other leagues
        else
            return;

        SportsResponse response = request.send();
        if(subscription.getCompletedGames() == null) {
            subscription.setCompletedGames(new boolean[response.getGames().length]);
            logger.debug("Team {} has {} games today", subscription.getTeamId(), response.getGames().length);
        }

        for(int i = 0; i < subscription.getCompletedGames().length; i++) {
            if(!subscription.getCompletedGames()[i]) {
                //Check the game to see if it's over
                if(response.getGames()[i].isFinished()) {
                    subscription.getCompletedGames()[i] = true;
                    if(subscription.getTeamId() == response.getGames()[i].winnerTeamId()) {
                        //If the team won, execute the win condition
                        logger.debug("Team {} won!", subscription.getTeamId());
                        subscription.executeWinCondition();
                    }
                    else {
                        logger.debug("Team {} lost", subscription.getTeamId());
                    }

                } else {
                    //If the game isn't over, schedule the next request based off the estimated time remaining
                    Date nextCheck = response.getGames()[i].estimatedEndTime();
                    logger.debug("Game for team {} not finished yet. Scheduling check for {}", subscription.getTeamId(), nextCheck);
                    subscription.getTimer().schedule(new SubscriptionChecker(subscription), nextCheck);
                }
            }
        }

        boolean allDone = true;
        for(boolean b : subscription.getCompletedGames()) {
            allDone = b && allDone;
        }

        //If all the games are over for today, schedule a check for tomorrow's games (12:01 a.m. tomorrow morning)
        if(allDone) {
            logger.debug("All of today's games completed for team {}. Scheduling check for tomorrow morning.", subscription.getTeamId());
            subscription.setCompletedGames(null);
            LocalTime midnight = LocalTime.MIDNIGHT;
            Date tomorrow = Date.from(LocalDateTime.of(LocalDate.now(), midnight).plusDays(1).plusMinutes(1).toInstant(OffsetDateTime.now().getOffset()));
            subscription.getTimer().schedule(new SubscriptionChecker(subscription), tomorrow);
        }
    }
}
