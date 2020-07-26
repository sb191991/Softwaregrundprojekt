package com.notimetospy.model.NetworkStandard.MessageType;

public enum MessageTypeEnum {

    //Spieliitialisierung
    HELLO,
    HELLO_REPLY,
    RECONNECT,
    GAME_STARTED,

    // Wahlphase
    REQUEST_ITEM_CHOICE,
    ITEM_CHOICE,
    REQUEST_EQUIPMENT_CHOICE,
    EQUIPMENT_CHOICE,

    // Spielphase
    GAME_STATUS,
    REQUEST_GAME_OPERATION,
    GAME_OPERATION,

    // Spielende
    STATISTICS,

    // Kontrollnachrichten
    GAME_LEAVE,
    GAME_LEFT,
    REQUEST_GAME_PAUSE,
    GAME_PAUSE,
    REQUEST_META_INFORMATION,
    META_INFORMATION,
    STRIKE,
    ERROR,

    // Optionale Komponenten
    REQUEST_REPLAY,
    REPLAY

}
