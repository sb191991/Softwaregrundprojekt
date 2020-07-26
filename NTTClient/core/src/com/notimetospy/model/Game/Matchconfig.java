package com.notimetospy.model.Game;

import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import javafx.collections.ObservableArrayBase;

import java.util.Map;

public class Matchconfig {

    /* Gadget: Moledie */
    private int moledieRange;

    /* Gadget: BowlerBlade */
    private int bowlerBladeRange;
    private double bowlerBladeHitChance;
    private int bowlerBladeDamage;

    /* Gadget: LaserCompact */
    private double laserCompactHitChance;

    /* Gadget: RocketPen */
    private int rocketPenDamage;

    /* Gadget: GasGloss */
    private int gasGlossDamage;

    /* Gadget: MothballPouch */
    private int mothballPouchRange;
    private int mothballPouchDamage;

    /* Gadget: FogTin */
    private int fogTinRange;

    /* Gadget: Grapple */
    private int grappleRange;
    private double grappleHitChance;

    /* Gadget: WiretapWithEarplugs */
    private double wiretapWithEarplugsFailChance;

    /* Gadget: Mirror */
    private double mirrorSwapChance;

    /* Gadget: Cocktail */
    private double cocktailDodgeChance;
    private int cocktailHp;

    /* Aktionen */
    private double spySuccessChance;
    private double babysitterSuccessChance;
    private double honeyTrapSuccessChance;
    private double observationSuccessChance;

    /* Spielfaktoren */
    private int chipsToIpFactor;
    private int secretToIpFactor;
    private int minChipsRoulette;
    private int maxChipsRoulette;
    private int roundLimit;
    private int turnPhaseLimit;
    private int catIp;
    private int strikeMaximum;
    private int pauseLimit;
    private int reconnectLimit;

    public Matchconfig(int moledieRange, int bowlerBladeRange, double bowlerBladeHitChance,
                       int bowlerBladeDamage, double laserCompactHitChance, int rocketPenDamage,
                       int gasGlossDamage, int mothballPouchRange, int mothballPouchDamage,
                       int fogTinRange, int grappleRange, double grappleHitChance,
                       double wiretapWithEarplugsFailChance, double mirrorSwapChance,
                       double cocktailDodgeChance, int cocktailHp, double spySuccessChance,
                       double babysitterSuccessChance, double honeyTrapSuccessChance,
                       double observationSuccessChance, int chipsToIpFactor, int secretToIpFactor,
                       int minChipsRoulette, int maxChipsRoulette, int roundLimit, int turnPhaseLimit,
                       int catIp, int strikeMaximum, int pauseLimit, int reconnectLimit) {

        this.moledieRange = moledieRange;
        this.bowlerBladeRange = bowlerBladeRange;
        this.bowlerBladeHitChance = bowlerBladeHitChance;
        this.bowlerBladeDamage = bowlerBladeDamage;
        this.laserCompactHitChance = laserCompactHitChance;
        this.rocketPenDamage = rocketPenDamage;
        this.gasGlossDamage = gasGlossDamage;
        this.mothballPouchRange = mothballPouchRange;
        this.mothballPouchDamage = mothballPouchDamage;
        this.fogTinRange = fogTinRange;
        this.grappleRange = grappleRange;
        this.grappleHitChance = grappleHitChance;
        this.wiretapWithEarplugsFailChance = wiretapWithEarplugsFailChance;
        this.mirrorSwapChance = mirrorSwapChance;
        this.cocktailDodgeChance = cocktailDodgeChance;
        this.cocktailHp = cocktailHp;
        this.spySuccessChance = spySuccessChance;
        this.babysitterSuccessChance = babysitterSuccessChance;
        this.honeyTrapSuccessChance = honeyTrapSuccessChance;
        this.observationSuccessChance = observationSuccessChance;
        this.chipsToIpFactor = chipsToIpFactor;
        this.secretToIpFactor = secretToIpFactor;
        this.minChipsRoulette = minChipsRoulette;
        this.maxChipsRoulette = maxChipsRoulette;
        this.roundLimit = roundLimit;
        this.turnPhaseLimit = turnPhaseLimit;
        this.catIp = catIp;
        this.strikeMaximum = strikeMaximum;
        this.pauseLimit = pauseLimit;
        this.reconnectLimit = reconnectLimit;
    }

    public void setMatchConfig(Map<String,Object> information){

        if(information.containsKey(moledieRange))
            this.moledieRange = (int) information.get(moledieRange);
        if(information.containsKey(bowlerBladeRange))
            this.bowlerBladeRange = (int) information.get(bowlerBladeRange);
        if(information.containsKey(bowlerBladeHitChance))
            this.bowlerBladeHitChance = (double) information.get(bowlerBladeHitChance);
        if(information.containsKey(bowlerBladeDamage))
            this.bowlerBladeDamage = (int) information.get(bowlerBladeDamage);
        if(information.containsKey(laserCompactHitChance))
            this.laserCompactHitChance = (double) information.get(laserCompactHitChance);
        if(information.containsKey(rocketPenDamage))
            this.rocketPenDamage = (int) information.get(rocketPenDamage);
        if(information.containsKey(gasGlossDamage))
            this.gasGlossDamage = (int) information.get(gasGlossDamage);
        if(information.containsKey(mothballPouchRange))
            this.mothballPouchRange = (int) information.get(mothballPouchRange);
        if(information.containsKey(mothballPouchDamage))
            this.mothballPouchDamage = (int) information.get(mothballPouchRange);
        if(information.containsKey(fogTinRange))
            this.fogTinRange = (int) information.get(fogTinRange);
        if(information.containsKey(grappleRange))
            this.grappleRange = (int) information.get(grappleRange);
        if(information.containsKey(grappleHitChance))
            this.grappleHitChance = (double) information.get(grappleHitChance);
        if(information.containsKey(wiretapWithEarplugsFailChance))
            this.wiretapWithEarplugsFailChance = (double) information.get(wiretapWithEarplugsFailChance);
        if(information.containsKey(mirrorSwapChance))
            this.mirrorSwapChance = (double) information.get(mirrorSwapChance);
        if(information.containsKey(cocktailDodgeChance))
            this.cocktailDodgeChance = (double) information.get(cocktailDodgeChance);
        if(information.containsKey(cocktailHp))
            this.cocktailDodgeChance = (double) information.get(cocktailHp);
        if(information.containsKey(spySuccessChance))
            this.spySuccessChance = (double) information.get(spySuccessChance);
        if(information.containsKey(babysitterSuccessChance))
            this.babysitterSuccessChance = (double) information.get(babysitterSuccessChance);
        if(information.containsKey(honeyTrapSuccessChance))
            this.honeyTrapSuccessChance = (double) information.get(honeyTrapSuccessChance);
        if(information.containsKey(observationSuccessChance))
            this.observationSuccessChance = (double) information.get(observationSuccessChance);
        if(information.containsKey(chipsToIpFactor))
            this.chipsToIpFactor = (int) information.get(chipsToIpFactor);
        if(information.containsKey(secretToIpFactor))
            this.secretToIpFactor = (int) information.get(secretToIpFactor);
        if(information.containsKey(minChipsRoulette))
            this.minChipsRoulette = (int) information.get(minChipsRoulette);
        if(information.containsKey(maxChipsRoulette))
            this.maxChipsRoulette = (int) information.get(maxChipsRoulette);
        if(information.containsKey(roundLimit))
            this.roundLimit = (int) information.get(roundLimit);
        if(information.containsKey(turnPhaseLimit))
            this.turnPhaseLimit = (int) information.get(turnPhaseLimit);
        if(information.containsKey(catIp))
            this.catIp = (int) information.get(catIp);
        if(information.containsKey(strikeMaximum))
            this.strikeMaximum = (int) information.get(strikeMaximum);
        if(information.containsKey(pauseLimit))
            this.pauseLimit = (int) information.get(pauseLimit);
        if(information.containsKey(reconnectLimit))
            this.reconnectLimit = (int) information.get(reconnectLimit);

    }

    public int getRange (GadgetEnum gadget) {
        if (gadget.equals(GadgetEnum.MOLEDIE)) {
            return moledieRange;
        } else if (gadget.equals(GadgetEnum.BOWLER_BLADE)) {
            return bowlerBladeRange;
        } else if (gadget.equals(GadgetEnum.MOTHBALL_POUCH)) {
            return mothballPouchRange;
        } else if (gadget.equals(GadgetEnum.FOG_TIN)){
            return fogTinRange;
        } else if (gadget.equals(GadgetEnum.GRAPPLE)) {
            return grappleRange;
        }
        return 0;
    }
}
