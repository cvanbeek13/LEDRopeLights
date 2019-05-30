package com.CoryVanBeek.RopeLights.sports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            request = new BaseballGamesRequest(subscription.getTeamId(), date);
        else if(Subscription.LEAGUE_HOCKEY.equalsIgnoreCase(subscription.getLeague()))
            request = new HockeyGamesRequest(subscription.getTeamId(), date);
        //TODO: Add other leagues
        else
            return;

        SportsResponse response;
        try {
            response = request.send();
        } catch(IOException e) {
            logger.warn("Unable to receive response for team " + subscription.getTeamId() + ".  Will try again in two minutes.", e);
            subscription.getTimer().schedule(new SubscriptionCheckerGame(subscription, date, gameID), new Date(new Date().getTime() + 120000));
            return;
        }

        logger.trace("Getting game with id {}", gameID);
        SportsGame game = response.getGame(gameID);

        if(game == null) {
            logger.warn("Game is null");
            return;
        }

        //Check the game to see if it's over
        if(game.isFinished()) {
            logger.trace("Game {} over.", gameID);
            if(subscription.getTeamId() == game.winnerTeamId()) {
                //If the team won, execute the win condition
                logger.info("Team {} won!", subscription.getTeamId());
                subscription.executeWinCondition();
            }
            else {
                logger.info("Team {} lost", subscription.getTeamId());
            }
        }
        else {
            //If the game isn't over, schedule the next request based off the estimated time remaining
            Date earliest = new Date(System.currentTimeMillis() + 30000);
            Date nextCheck = game.estimatedEndTime();
            if(earliest.after(nextCheck))
                nextCheck = earliest;

            logger.debug("Game for team {} not finished yet. Scheduling check for {}", subscription.getTeamId(), nextCheck);
            subscription.getTimer().schedule(new SubscriptionCheckerGame(subscription, date, gameID), nextCheck);
        }
    }
}
