package com.CoryVanBeek.RopeLights.sports;

import java.util.Date;

/**
 * Class used to send requests for daily baseball game(s) data for the given team.
 */
class BaseballGamesRequest extends SportsRequest<BaseballGamesResponse> {

    BaseballGamesRequest(int teamId, Date date) {
        super(BaseballGamesResponse.class);
        this.teamId = teamId;
        this.date = date;
        season = "latest";
    }

    @Override
    String getURL() {
        return String.format("https://api.mysportsfeeds.com/v2.0/pull/mlb/%s/date/%s/games.json?team=%d", season, format.format(date), teamId);
    }
}
