package com.notimetospy.controller.Gadget;

import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;

public class Gadget {

    public GadgetEnum gadget;
    private int usages;

    public Gadget(GadgetEnum gadget, int usages) {
        this.gadget = gadget;
        this.usages = usages;
    }

}
