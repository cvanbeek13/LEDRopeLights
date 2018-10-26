package com.CoryVanBeek.RopeLights.sports;

/**
 * An interface for parsing responses from the MySportsFeed Daily Games API
 *
 * @author Cory Van Beek
 */
public interface SportsResponse {

    /**
     * Parses the JSON Response from the sports request into the meaningful information for the give sport.
     *
     * @param jsonString The JSON string to parse from the response
     */
    void parse(String jsonString);

    /**
     * Gets the SportsGames in the response
     * @return
     */
    SportsGame[] getGames();

    /**
     * Gets the SportsGame with the specific MySportsFeed Game ID from the response
     *
     * @param id The MySportsFeed Game ID
     * @return The SportsGame instance
     */
    SportsGame getGame(int id);
}
