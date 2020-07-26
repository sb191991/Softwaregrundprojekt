package com.notimetospy.model.Game;

import com.notimetospy.model.Character.Character;
import com.notimetospy.model.Field.FieldMap;
import com.notimetospy.model.Game.Point;

import java.util.Set;

public class State {

    private int currentRound;
    private  FieldMap map;
    private Set<Integer> mySafeCombinations;
    public Set<Character> characters;
    private Point catCoordinates;
    private Point janitorCoordinates;

    public State(int currentRound, FieldMap map, Set<Integer> mySafeCombinations,
                 Set<Character> characters, Point catCoordinates, Point janitorCoordinates){

        this.currentRound = currentRound;
        this.map = map;
        this.mySafeCombinations = mySafeCombinations;
        this.characters = characters;
        this.catCoordinates = catCoordinates;
        this.janitorCoordinates = janitorCoordinates;
    }

    public  FieldMap getMap() {
        return map;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public Set<Integer> getMySafeCombinations() {
        return mySafeCombinations;
    }

    public Set<Character> getCharacters() {
        return characters;
    }

    public Point getCatCoordinates() {
        return catCoordinates;
    }

    public Point getJanitorCoordinates() {
        return janitorCoordinates;
    }
}
