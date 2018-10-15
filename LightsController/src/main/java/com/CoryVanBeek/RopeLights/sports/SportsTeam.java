package com.CoryVanBeek.RopeLights.sports;

import com.CoryVanBeek.RopeLights.Color;

/**
 * An object used to hold relevant information about a sports team
 *
 * @author Cory Van Beek
 */
public class SportsTeam {
    private int id;
    private String name;
    private String league;
    private Color[] teamColors;

    SportsTeam(int id, String name, String league, Color... colors) {
        this.id = id;
        this.name = name;
        this.league = league;

        teamColors = new Color[colors.length];
        System.arraycopy(colors, 0, teamColors, 0, teamColors.length);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLeague() {
        return league;
    }

    public Color[] getTeamColors() {
        return teamColors;
    }
}
