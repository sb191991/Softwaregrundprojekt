package com.notimetospy.model.Character;

import com.notimetospy.model.NetworkStandard.GameEnums.GenderEnum;
import com.notimetospy.model.NetworkStandard.GameEnums.PropertyEnum;

import java.util.List;
import java.util.UUID;

public class CharacterInformation extends CharacterDescription {

    public UUID characterId;

    public CharacterInformation(UUID characterId, String name, String description, GenderEnum gender,
                                List<PropertyEnum> features) {
        super(name, description, gender, features);
        this.characterId = characterId;
    }


}
