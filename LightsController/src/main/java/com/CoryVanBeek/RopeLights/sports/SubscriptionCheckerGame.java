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
 * for a specific game
 *
 * @author Cory Van Beek
 */
public class SubscriptionCheckerGame extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionCheckerGame.class);

    private Subscription subscription;
    private Date date;
    private int gameID;

    SubscriptionCheckerGame(Subscription subscription, Date date, int gameID) {
        this.subscription = subscription;
        this.date = date;
        this.gameID = gameID;
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
        SportsGame game = response.getGame(gameID);
        subscription.updateTrackedGame(game);

        //Check the game to see if it's over
        if(game.isFinished()) {
            if(subscription.getTeamId() == game.winnerTeamId()) {
                //If the team won, execute the win condition
                logger.info("Team {} won!", subscription.getTeamId());
                subscription.executeWinCondition();
            }
            else {
                logger.info("Team {} lost", subscription.getTeamId());
            }
            subscription.removeTrackedGame(gameID);
        } else {
            //If the game isn't over, schedule the next request based off the estimated time remaining
            Date nextCheck = game.estimatedEndTime();
            logger.debug("Game for team {} not finished yet. Scheduling check for {}", subscription.getTeamId(), nextCheck);
            subscription.getTimer().schedule(new SubscriptionCheckerGame(subscription, date, gameID), nextCheck);
        }
    }
}
