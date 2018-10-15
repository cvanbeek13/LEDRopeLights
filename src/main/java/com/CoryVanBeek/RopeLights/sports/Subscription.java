package com.CoryVanBeek.RopeLights.sports;

import com.CoryVanBeek.RopeLights.LightsBridge;
import com.CoryVanBeek.RopeLights.LightsProperties;
import com.CoryVanBeek.RopeLights.thing.PropertiesHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * This class is used to subscribe to game updates using the MySportsFeed API.
 *
 * @author Cory Van Beek
 */
public class Subscription {
    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);
    static final String LEAGUE_BASEBALL = "mlb";

    private LightsBridge bridge;

    private String league;
    private PropertiesHolder winState;
    private int teamId;
    private boolean[] completedGames;
    private Timer timer;

    Subscription(SportsTeam team, LightsBridge bridge, LightsProperties properties) {
        this.league = team.getLeague();
        this.teamId = team.getId();
        this.bridge = bridge;

        this.winState = new PropertiesHolder();
        //TODO: Make the winState based off of defaults in properties that could be overwritten in the properties file
        winState.colors = team.getTeamColors();
        winState.deltaTime = 2000;
        winState.mode = "Sin";
        winState.power = "on";
        winState.duration = 1000 * 60 * 60;

        this.timer = new Timer();
        timer.schedule(new SubscriptionChecker(this), 5000);
        logger.info("Subscribed to {} team {}", league, teamId);
    }

    /**
     * Unsubscribes to the sports team and cancels all future game updates.
     */
    public void unsubscribe() {
        timer.cancel();
        logger.info("Unsubscribed from {} team {}", league, teamId);
    }

    public void executeWinCondition() {
        bridge.sendDesired(winState);
    }

    public int getTeamId() {
        return teamId;
    }

    public String getLeague() {
        return league;
    }

    public boolean[] getCompletedGames() {
        return completedGames;
    }

    public void setCompletedGames(boolean[] completedGame) {
        this.completedGames = completedGame;
    }

    public Timer getTimer() {
        return timer;
    }
}