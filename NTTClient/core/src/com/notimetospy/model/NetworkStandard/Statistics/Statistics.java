package com.notimetospy.model.NetworkStandard.Statistics;

public class Statistics {

    private StatisticsEntry[] entries;

    public Statistics(StatisticsEntry[] entries) {
        this.entries = new StatisticsEntry[entries.length];
    }

    public StatisticsEntry[] getEntries(){
        return this.entries;
    }
}
