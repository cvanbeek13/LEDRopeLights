package com.CoryVanBeek.RopeLights.sports;

import com.CoryVanBeek.RopeLights.LightsBridge;
import com.CoryVanBeek.RopeLights.LightsProperties;
import com.CoryVanBeek.RopeLights.thing.PropertiesHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Timer;

/**
 * This class is used to subscribe to game updates using the MySportsFeed API.
 *
 * @author Cory Van Beek
 */
public class Subscription {
    private static final Logger logger = LoggerFactory.getLogger(Subscription.class);
    static final String LEAGUE_BASEBALL = "mlb";
    static final String LEAGUE_HOCKEY = "nhl";

    private LightsBridge bridge;

    private String league;
    private PropertiesHolder winState;
    private int teamId;
    private Timer timer;


    Subscription(SportsTeam team, LightsBridge bridge, LightsProperties properties) {
        this.league = team.getLeague();
        this.teamId = team.getId();
        this.bridge = bridge;

        this.winState = new PropertiesHolder();
        //TODO: Make the winState based off of defaults in properties that could be overwritten in the properties file
        winState.colors = team.getTeamColors();
        winState.deltaTime = 4000L;
        winState.mode = "sin";
        winState.power = "on";
        winState.duration = 1000L * 60L * 60L;
        winState.teams = null;

        this.timer = new Timer();
        timer.schedule(new SubscriptionCheckerDaily(this, new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)), 5000);
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

    public Timer getTimer() {
        return timer;
    }
}
