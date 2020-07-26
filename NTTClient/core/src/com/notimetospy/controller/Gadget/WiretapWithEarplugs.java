package com.notimetospy.controller.Gadget;

import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;

import java.util.UUID;

public class WiretapWithEarplugs extends Gadget {

    private boolean working;
    private UUID activeOn;

    public WiretapWithEarplugs(GadgetEnum gadget, int usages, boolean working, UUID activeOn) {
        super(gadget, usages);

        this.working = working;
        this.activeOn = activeOn;
    }
}
