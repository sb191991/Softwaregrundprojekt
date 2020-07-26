package com.notimetospy.model.Character;

import com.notimetospy.controller.Gadget.Cocktail;
import com.notimetospy.controller.Gadget.Gadget;
import com.notimetospy.controller.MessageReceiver;
import com.notimetospy.model.Game.Point;
import com.notimetospy.model.Game.State;
import com.notimetospy.model.NetworkStandard.GameEnums.GadgetEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.PropertyEnum;
import com.notimetospy.view.GameScreen;

import javax.xml.stream.events.Characters;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class Character {

    public UUID characterId;
    private String name;
    public Point coordinates;
    private int mp,ap,hp,ip,chips;
    private Set<PropertyEnum> properties;
    public Set<Gadget> gadgets;

    public Character(UUID characterId, String name, Point coordinates,
                     int mp, int ap, int hp, int ip, int chips,
                     Set<PropertyEnum> properties, Set<Gadget> gadgets) {

        this.characterId = characterId;
        this.name = name;
        this.coordinates = coordinates;
        this.mp = mp;
        this.ap = ap;
        this.hp = hp;
        this.ip = ip;
        this.chips = chips;
        this.properties = properties;
        this.gadgets = gadgets;
    }

    public Point getCoordinates(){
        return coordinates;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public Set<Gadget> getGadgets() {
        return gadgets;
    }

    public int getMp() {
        return  this.mp;
    }

    public int getAp() {
        return this.ap;
    }

    public int getHp() {
        return this.hp;
    }

    public int getIp() {
        return this.ip;
    }

    public int getChips() {
        return this.chips;
    }

    public Set<PropertyEnum> getProperties() {
        return this.properties;
    }

    public GadgetEnum[] getGadgetsAsEnum() {
        GadgetEnum[] gadgetList = new GadgetEnum[gadgets.size()];
        Iterator itr = this.gadgets.iterator();
        int index = 0;
        while (itr.hasNext()) {
            Gadget gadget = (Gadget) itr.next();
            gadgetList[index] = gadget.gadget;
            index++;
        }
        return gadgetList;
    }

    @Override
    public String toString() {
        return "Character Info{" +
                "Name='" + name + '\n' +
                ", Moving Points = " + mp + '\n' +
                ", Action Points = " + ap + '\n' +
                ", Health Points = " + hp + '\n' +
                ", Intelligence Points = " + ip + '\n' +
                ", Chips = " + chips +
                '}';
    }

    public boolean hasCocktail() {
        boolean hasCocktail = false;
        Iterator itr = gadgets.iterator();
        while(itr.hasNext()) {
            Gadget gadget = (Gadget) itr.next();
            if (gadget.gadget.equals(GadgetEnum.COCKTAIL)) {
                hasCocktail = true;
            }
        }
        return hasCocktail;
    }

    public boolean hasDiamondCollar() {
        boolean hasDiamondCollar = false;
        Iterator itr = gadgets.iterator();
        while(itr.hasNext()) {
            Gadget gadget = (Gadget) itr.next();
            if (gadget.gadget.equals(GadgetEnum.DIAMOND_COLLAR)) {
                hasDiamondCollar = true;
            }
        }
        return hasDiamondCollar;
    }
}
