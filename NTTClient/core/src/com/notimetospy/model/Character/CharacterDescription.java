package com.notimetospy.model.Character;


import com.notimetospy.model.NetworkStandard.GameEnums.GenderEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.PropertyEnum;

import java.util.List;


public class CharacterDescription {

    public String name;
    public String description;
    private GenderEnum gender;
    private transient List<PropertyEnum> features;

    public CharacterDescription(String name, String description, GenderEnum gender,
                                List<PropertyEnum> features) {
        this.name = name;
        this.description = description;
        this.gender = gender;
        this.features = features;
    }

    @Override
    public String toString() {
        return  name;
    }
}
