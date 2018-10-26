package com.CoryVanBeek.RopeLights.sports;

import java.util.Date;

/**
 * Class used to send requests for daily hockey game(s) data for the given team.
 */
class HockeyGamesRequest extends SportsRequest<HockeyGamesResponse> {

    HockeyGamesRequest(int teamId, Date date) {
        super(HockeyGamesResponse.class);
        this.teamId = teamId;
        this.date = date;
        season = "current";
    }

    @Override
    String getURL() {
        return String.format("https://api.mysportsfeeds.com/v2.0/pull/nhl/%s/date/%s/games.json?team=%d", season, format.format(date), teamId);
    }
}
