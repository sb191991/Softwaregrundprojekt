package com.notimetospy.model.NetworkStandard.Messages;

import com.notimetospy.model.NetworkStandard.GameEnums.VictoryEnum;
import com.notimetospy.model.NetworkStandard.MessageContainer.MessageContainer;
import com.notimetospy.model.NetworkStandard.MessageType.MessageTypeEnum;
import com.notimetospy.model.NetworkStandard.Statistics.Statistics;

import java.util.Date;
import java.util.UUID;

public class StatisticsMessage extends MessageContainer {

    public Statistics statistics;
    public UUID winner;
    public VictoryEnum reason;
    public boolean hasReplay;


    public StatisticsMessage(MessageTypeEnum type, UUID clientId, String date, String debugMessage,
                             Statistics statistics, UUID winner, VictoryEnum reason,
                             boolean hasReplay) {
        super(type, clientId, date, debugMessage);
        this.statistics = statistics;
        this.winner = winner;
        this.reason = reason;
        this.hasReplay = hasReplay;
    }
}
