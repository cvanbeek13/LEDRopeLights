package com.CoryVanBeek.RopeLights.sports;

import java.util.Date;

/**
 * An interface to get important information from the games returned in the MySportsFeed response
 *
 * @author Cory Van Beek
 */
public interface SportsGame {

    /**
     * Checks to see if the game is finished
     *
     * @return True if the games is complete
     */
    boolean isFinished();

    /**
     * Checks for the winner of the game
     *
     * @return -1 if the game isn't over, -2 for a tie, otherwise the id of the winning team
     */
    int winnerTeamId();

    /**
     * Estimates the (minimumish) amount of time remaining in the game to know when to request data again.
     *
     * @return The Date value when the game is expected to be over
     */
    Date estimatedEndTime();
}
