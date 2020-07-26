package com.notimetospy.controller.Action;

import com.notimetospy.controller.Operation.Operation;
import com.notimetospy.model.Game.Point;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.OperationEnum;

import java.util.UUID;

public class GadgetAction extends Operation {

    private GadgetEnum gadget;

    public GadgetAction(GadgetEnum gadget, OperationEnum type, boolean successful, Point target, UUID characterId) {
        super(type, successful,target, characterId);
        this.gadget = gadget;
    }

    @Override
    /**
     * Diese Methode wird verwendet, um im GameScreen Gadget-Operationen anzuzeigen. Der ausfuehrende
     * Charakter sowie ggf ein Zielcharakter muessen noch separat bestimmt und angezeigt werden.
     * @return
     */
    public String toString() {
        switch(gadget) {
            case NUGGET:
                if (super.isSuccessful()) {
                    return "Bestechung durch Nugget erfolgreich: Charakter ";
                } else {
                    return "Bestechung durch Nugget gescheitert: Charakter ";
                }
            case FOG_TIN:
                return "Wurf der Nebeldose auf Feld " + super.getTarget().toString() + ": ";
            case GRAPPLE:
                if (super.isSuccessful()) {
                    return "Einsatz von Wurfhaken auf Feld" + super.getTarget().toString() + " erfolgreich: Charakter ";
                } else {
                    return "Einsatz von Wurfhaken auf Feld" + super.getTarget().toString() + " gescheitert: Charakter ";
                }
            case JETPACK:
                return "Flug mit Jetpack auf Feld " + super.getTarget().toString() + ": Charakter ";
            case MOLEDIE:
                return "Maulwürfelwurf auf Feld " + super.getTarget().toString() + ": Charakter ";
            case COCKTAIL://nur Cocktailguss, schluerfen muss separat behandelt werden
                if (super.isSuccessful()) {
                    return "Cocktailguss erfolgreich: Charakter ";
                } else {
                    return "Cocktailguss gescheitert: Charakter ";
                }
            case GAS_GLOSS:
                return "Einsatz von Gaspatronen-Lippenstift: Charakter ";
            case HAIRDRYER:
                return "Trockenföhnen mit Akku-Föhn: Charakter ";
            case ROCKET_PEN:
                return "Einsatz von Raketenwerfer-Füllfederhalter gegen Feld " + super.getTarget().toString() + "Charakter ";
            case BOWLER_BLADE:
                if (super.isSuccessful()) {
                    return "Klingenhut-Wurf erfolgreich: Charakter ";
                } else {
                    return "Klingenhut-Wurf gescheitert: Charakter ";
                }
            case CHICKEN_FEED:
                return "Chicken Feed gegeben: Charakter ";
            case POISON_PILLS:
                return "Cocktail auf Feld " + super.getTarget().toString() + "mit Giftpillen-Flasche vergiftet: Charakter ";
            case LASER_COMPACT:
                return "Cocktail auf Feld" + super.getTarget().toString() + "mit Laser-Puderdose zersört: Charakter";
            case MOTHBALL_POUCH:
                return "Mottenkugel-Wurf auf Feld " + super.getTarget().toString() + ": Charakter";
            case TECHNICOLOUR_PRISM:
                return "Roulette-Tisch auf Feld " + super.getTarget().toString() + " mit Technicolor-Prisma invertiert: Charakter ";
            case MIRROR_OF_WILDERNESS:
                if (super.isSuccessful()) {
                    return "IP-Tausch durch Mirror-of-Wilderness erfolgreich: Charakter ";
                } else {
                    return "IP-Tausch durch Mirror-of-Wilderness gescheitert: Charakter ";
                }
            case WIRETAP_WITH_EARPLUGS:
                return "Charakter durch Wanze mit Ohrstöpsel verwanzt: Charakter ";
        }
        return "";
    }

    public GadgetEnum getGadget() {
        return gadget;
    }
}
