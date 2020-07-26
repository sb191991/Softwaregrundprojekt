package com.notimetospy.model.Game;

import com.notimetospy.controller.ConnectionHandler;
import com.notimetospy.controller.MessageReceiver;
import com.notimetospy.model.Field.FieldStateEnum;

public class Scenario {

    private FieldStateEnum[][] scenario;

    public Scenario(FieldStateEnum[][] scenario){
        this.scenario = new FieldStateEnum[7][7];
    }

    public FieldStateEnum[][] getScenario() {
        return scenario;
    }

    private void updateScenario(){

    }
}
