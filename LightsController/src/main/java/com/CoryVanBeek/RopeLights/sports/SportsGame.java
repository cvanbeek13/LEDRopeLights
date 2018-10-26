package com.CoryVanBeek.RopeLights.sports;

import java.util.Date;

/**
 * An interface to get important information from the games returned in the MySportsFeed response
 *
 * @author Cory Van Beek
 */
public interface SportsGame {
    String COMPLETED = "COMPLETED";
    String COMPLETED_PENDING_REVIEW = "COMPLETED_PENDING_REVIEW";
    String LIVE = "LIVE";
    String RESPONSE_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * Gets the MySportsFeed game ID
     *
     * @return The game ID
     */
    int getGameID();

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
