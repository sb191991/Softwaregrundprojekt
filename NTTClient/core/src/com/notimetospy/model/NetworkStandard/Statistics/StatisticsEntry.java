package com.notimetospy.model.NetworkStandard.Statistics;

public class StatisticsEntry {

    private String title;
    private String description;
    private String valuePlayer1;
    private String getValuePlayer2;

    public StatisticsEntry(String title, String description, String valuePlayer1, String getValuePlayer2) {
        this.title = title;
        this.description = description;
        this.valuePlayer1 = valuePlayer1;
        this.getValuePlayer2 = getValuePlayer2;
    }
}
