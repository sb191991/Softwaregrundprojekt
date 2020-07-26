package com.notimetospy.controller.Gadget;

import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;

public class Cocktail extends Gadget {

    private boolean isPoisoned;

    public Cocktail(GadgetEnum gadget, int usages, boolean isPoisoned) {
        super(gadget, usages);

        this.isPoisoned = isPoisoned;
    }
}
