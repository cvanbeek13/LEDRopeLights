package com.CoryVanBeek.RopeLights.sports;

import com.CoryVanBeek.RopeLights.Color;
import com.CoryVanBeek.RopeLights.LightsBridge;
import com.CoryVanBeek.RopeLights.LightsProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class manages the subscriptions received from the Iot thing shadow.
 * It subscribes and unsubscribes to teams when changes are made to the shadow.
 *
 * @author Cory Van Beek
 */
public class SubscriptionManager {
    private HashMap<Integer, Subscription> subscriptions;
    private HashMap<Integer, SportsTeam> allTeams;
    private LightsBridge bridge;
    private LightsProperties properties;

    public SubscriptionManager(LightsBridge bridge, LightsProperties properties) {
        this.bridge = bridge;
        this.properties = properties;
        subscriptions = new HashMap<>();
        allTeams = new HashMap<>();

        allTeams.put(140, new SportsTeam(140, "Arizona Diamondbacks", "mlb", new Color(167,25,48), new Color(227,212,173)));
        allTeams.put(130, new SportsTeam(130, "Atlanta Braves", "mlb", new Color(206,17,65), new Color(19, 39, 79)));
        allTeams.put(111, new SportsTeam(111, "Baltimore Orioles", "mlb", new Color(223,70,1)));
        allTeams.put(113, new SportsTeam(113, "Boston Red Sox", "mlb", new Color(189, 48, 57), new Color(12,35,64)));
        allTeams.put(131, new SportsTeam(131, "Chicago Cubs", "mlb", new Color(14,51,134), new Color(204,52,51)));
        allTeams.put(119, new SportsTeam(119, "Chicago White Sox", "mlb", new Color(39,37,31), new Color(196,206,212)));
        allTeams.put(135, new SportsTeam(135, "Cincinnati Reds", "mlb", new Color(198,1,31), new Color(255,255,255)));
        allTeams.put(116, new SportsTeam(116, "Cleveland Indians", "mlb", new Color(12,35,64), new Color(227,25,55)));
        allTeams.put(138, new SportsTeam(138, "Colorado Rockies", "mlb", new Color(51,0,111), new Color(196,206,212)));
        allTeams.put(117, new SportsTeam(117, "Detroit Tigers", "mlb", new Color(12,35,64), new Color(250,70,22)));
        allTeams.put(122, new SportsTeam(122, "Houston Astros", "mlb", new Color(0,45,98), new Color(235,110,31)));
        allTeams.put(118, new SportsTeam(118, "Kansas City Royals", "mlb", new Color(0,20,137), new Color(192,154,91)));
        allTeams.put(124, new SportsTeam(124, "Los Angeles Angels", "mlb", new Color(0,50,99), new Color(186,0,33)));
        allTeams.put(137, new SportsTeam(137, "Los Angeles Dodgers", "mlb", new Color(0,90,156), new Color(239,62,66)));
        allTeams.put(128, new SportsTeam(128, "Miami Marlins", "mlb", new Color(255,102,0), new Color(0,119,200)));
        allTeams.put(134, new SportsTeam(134, "Milwaukee Brewers", "mlb", new Color(19,41,75), new Color(182,146,46)));
        allTeams.put(120, new SportsTeam(120, "Minnesota Twins", "mlb", new Color(0,43,92), new Color(211,17,69)));
        allTeams.put(127, new SportsTeam(127, "New York Mets", "mlb", new Color(0,45, 114), new Color(252,89,16)));
        allTeams.put(114, new SportsTeam(114, "New York Yankees", "mlb", new Color(12,35,64), new Color(255,255,255)));
        allTeams.put(125, new SportsTeam(125, "Oakland Athletics", "mlb", new Color(0,56,49), new Color(239,178,30)));
        allTeams.put(129, new SportsTeam(129, "Philadelphia Phillies", "mlb", new Color(232,24,40), new Color(40,72,152)));
        allTeams.put(132, new SportsTeam(132, "Pittsburgh Pirates", "mlb", new Color(39,37,31), new Color(253,184,39)));
        allTeams.put(139, new SportsTeam(139, "San Diego Padres", "mlb", new Color(0,45,98), new Color(162,170,173)));
        allTeams.put(136, new SportsTeam(136, "San Francisco Giants", "mlb", new Color(253,90,30), new Color(39,37,31)));
        allTeams.put(123, new SportsTeam(123, "Seattle Mariners", "mlb", new Color(12,44,86), new Color(0,92,92), new Color(196,206,212)));
        allTeams.put(133, new SportsTeam(133, "St. Louis Cardinals", "mlb", new Color(196,30,58), new Color(12,35,64)));
        allTeams.put(115, new SportsTeam(115, "Tampa Bay Rays", "mlb", new Color(9,44,92), new Color(143,188,230)));
        allTeams.put(121, new SportsTeam(121, "Texas Rangers", "mlb", new Color(0,50,120), new Color(192,17,31)));
        allTeams.put(112, new SportsTeam(112, "Toronto Blue Jays", "mlb", new Color(19,74,142), new Color(232,41,28)));
        allTeams.put(126, new SportsTeam(126, "Washington Nationals", "mlb", new Color(171,0,3), new Color(20,34,90)));

        //TODO: Add support for NHL, NFL, and NBA
    }

    public void manage(int[] teamIds) {
        Set<Integer> currentSubscriptions = new HashSet<>(subscriptions.keySet());
        if(teamIds != null) {
            for(int i: teamIds) {
                if(currentSubscriptions.contains(i)) {
                    currentSubscriptions.remove(i);
                }
                else {
                    Subscription sub = new Subscription(allTeams.get(i), bridge, properties);
                    subscriptions.put(i, sub);
                }
            }
        }

        for(int i: currentSubscriptions) {
            subscriptions.get(i).unsubscribe();
            subscriptions.remove(i);
        }

    }
}
