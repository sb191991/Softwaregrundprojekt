# NTTServer
NTTServer

## Functions

<dl>
<dt><a href="#sendMessage">sendMessage(ws, type, data, debugMessage)</a></dt>
<dd><p>Sends a message to a websocket client.</p>
</dd>
<dt><a href="#broadcastMessage">broadcastMessage(type, data, debugMessage)</a></dt>
<dd><p>Broadcasts a message to a all websocket clients.</p>
</dd>
<dt><a href="#endGame">endGame()</a></dt>
<dd><p>Ends the game.</p>
</dd>
<dt><a href="#pauseGame">pauseGame()</a></dt>
<dd><p>Pauses the game.</p>
</dd>
<dt><a href="#resumeGame">resumeGame()</a></dt>
<dd><p>Resumes the game.</p>
</dd>
<dt><a href="#startGame">startGame()</a></dt>
<dd><p>Sarts the game.</p>
</dd>
<dt><a href="#setGameCharacters">setGameCharacters(player, freeFields)</a></dt>
<dd><p>Initiates the characters of a player.</p>
</dd>
<dt><a href="#getGameState">getGameState(ws)</a> ⇒ <code>Object</code></dt>
<dd><p>Returns the current game state in respect to a specific client.</p>
</dd>
<dt><a href="#sendGameStatusMessage">sendGameStatusMessage()</a></dt>
<dd><p>Broadcasts the current game state.</p>
</dd>
<dt><a href="#startRound">startRound()</a></dt>
<dd><p>Starts a new round</p>
</dd>
<dt><a href="#setCharacterOrder">setCharacterOrder()</a></dt>
<dd><p>Sets the order of all characters.</p>
</dd>
<dt><a href="#startTurn">startTurn(characterId)</a></dt>
<dd><p>Starts a new turn for a specific character.</p>
</dd>
<dt><a href="#nextTurn">nextTurn()</a></dt>
<dd><p>Starts the next turn or next round if all characters had their turn.</p>
</dd>
<dt><a href="#prepareTurn">prepareTurn(characterId)</a></dt>
<dd><p>Prepares a characters turn and sets their mps and aps in respect to their properties.</p>
</dd>
<dt><a href="#getCharacterByPosition">getCharacterByPosition(coordinates)</a> ⇒ <code>Character</code></dt>
<dd><p>Returns a character (if on field) by position.</p>
</dd>
<dt><a href="#getChance">getChance(character, chance)</a> ⇒ <code>Boolean</code></dt>
<dd><p>Returns true or false for a character with a specific chance in respect to their properties.</p>
</dd>
<dt><a href="#damageCharacter">damageCharacter(character, damage, poison)</a></dt>
<dd><p>Adds damage to a character in respect to their properties. (Decreases hp)</p>
</dd>
<dt><a href="#giveCharIP">giveCharIP(character, ip)</a></dt>
<dd><p>Adds intelligence points to a character.</p>
</dd>
<dt><a href="#getGadget">getGadget(character, gadget)</a> ⇒ <code>Gadget</code></dt>
<dd><p>Returns specific gadget of a character.</p>
</dd>
<dt><a href="#removeGadget">removeGadget(character, type)</a></dt>
<dd><p>Removes a specific gadget from a character.</p>
</dd>
<dt><a href="#removeProperty">removeProperty(character, type)</a></dt>
<dd><p>Removes a specific property from a character.</p>
</dd>
<dt><a href="#getDistance">getDistance(pos1, pos2)</a> ⇒ <code>Integer</code></dt>
<dd><p>Returns the distance of to points.</p>
</dd>
<dt><a href="#getNearestCharacter">getNearestCharacter(coordinates, maxDist)</a> ⇒ <code>Character</code></dt>
<dd><p>Returns the nearest character to a specific point.</p>
</dd>
<dt><a href="#isNeighbor">isNeighbor(position, target, [range])</a> ⇒ <code>Boolean</code></dt>
<dd><p>Returns if one point is a neighbor of the other.</p>
</dd>
<dt><a href="#getNeighbors">getNeighbors(coordinates, [range])</a> ⇒ <code>Array.&lt;Point&gt;</code></dt>
<dd><p>Returns all neighbors for a specific point.</p>
</dd>
<dt><a href="#getRandomFreeNeighbor">getRandomFreeNeighbor(coordinates, [range])</a> ⇒ <code>Point</code></dt>
<dd><p>Returns a random free neighbor for a specific point.</p>
</dd>
<dt><a href="#getRandomNeighborCharacter">getRandomNeighborCharacter(coordinates, [range], [sight])</a> ⇒ <code>Character</code></dt>
<dd><p>Returns a random neighbor character for a specific point.</p>
</dd>
<dt><a href="#getRandomCharacter">getRandomCharacter()</a> ⇒ <code>Character</code></dt>
<dd><p>Returns a random character on the map.</p>
</dd>
<dt><a href="#isInSight">isInSight(position, target, chars)</a> ⇒ <code>Boolean</code></dt>
<dd><p>Return if two points are in sight.</p>
</dd>
<dt><a href="#isCoordinatesValid">isCoordinatesValid(coordinates)</a> ⇒ <code>Boolean</code></dt>
<dd><p>If point is on the map.</p>
</dd>
<dt><a href="#doOperation">doOperation(player, operation)</a> ⇒ <code>Boolean</code></dt>
<dd><p>Perfoms actions which a character can do with certain properties.</p>
</dd>
</dl>

<a name="sendMessage"></a>

## sendMessage(ws, type, data, debugMessage)
Sends a message to a websocket client.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| ws | <code>Websocket</code> | The receiving websocket client. |
| type | <code>messageTypeEnum</code> | The type of the message. |
| data | <code>Object</code> | The data to send. |
| debugMessage | <code>String</code> | The debug message to send. |

<a name="broadcastMessage"></a>

## broadcastMessage(type, data, debugMessage)
Broadcasts a message to a all websocket clients.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| type | <code>messageTypeEnum</code> | The type of the message. |
| data | <code>Object</code> | The data to send. |
| debugMessage | <code>String</code> | The debug message to send. |

<a name="endGame"></a>

## endGame()
Ends the game.

**Kind**: global function
<a name="pauseGame"></a>

## pauseGame()
Pauses the game.

**Kind**: global function
<a name="resumeGame"></a>

## resumeGame()
Resumes the game.

**Kind**: global function
<a name="startGame"></a>

## startGame()
Sarts the game.

**Kind**: global function
<a name="setGameCharacters"></a>

## setGameCharacters(player, freeFields)
Initiates the characters of a player.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| freeFields | <code>Array.&lt;Field&gt;</code> | The remaining free fields on the map. |

<a name="getGameState"></a>

## getGameState(ws) ⇒ <code>Object</code>
Returns the current game state in respect to a specific client.

**Kind**: global function
**Returns**: <code>Object</code> - The game state.

| Param | Type | Description |
| --- | --- | --- |
| ws | <code>Websocket</code> | The websocket client. |

<a name="sendGameStatusMessage"></a>

## sendGameStatusMessage()
Broadcasts the current game state.

**Kind**: global function
<a name="startRound"></a>

## startRound()
Starts a new round

**Kind**: global function
<a name="setCharacterOrder"></a>

## setCharacterOrder()
Sets the order of all characters.

**Kind**: global function
<a name="startTurn"></a>

## startTurn(characterId)
Starts a new turn for a specific character.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| characterId | <code>String</code> | The id of the character. |

<a name="nextTurn"></a>

## nextTurn()
Starts the next turn or next round if all characters had their turn.

**Kind**: global function
<a name="prepareTurn"></a>

## prepareTurn(characterId)
Prepares a characters turn and sets their mps and aps in respect to their properties.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| characterId | <code>String</code> | The id of the character. |

<a name="getCharacterByPosition"></a>

## getCharacterByPosition(coordinates) ⇒ <code>Character</code>
Returns a character (if on field) by position.

**Kind**: global function
**Returns**: <code>Character</code> - The character. (null if no character on that position)

| Param | Type | Description |
| --- | --- | --- |
| coordinates | <code>Point</code> | The position. |

<a name="getChance"></a>

## getChance(character, chance) ⇒ <code>Boolean</code>
Returns true or false for a character with a specific chance in respect to their properties.

**Kind**: global function
**Returns**: <code>Boolean</code> - Random boolean.

| Param | Type | Description |
| --- | --- | --- |
| character | <code>Character</code> | The character. |
| chance | <code>Double</code> | The chance. |

<a name="damageCharacter"></a>

## damageCharacter(character, damage, poison)
Adds damage to a character in respect to their properties. (Decreases hp)

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| character | <code>Character</code> | The character to damage. |
| damage | <code>Integer</code> | The amaount of damage. |
| poison | <code>Boolean</code> | If cause of damage is a poisoned cocktail. |

<a name="giveCharIP"></a>

## giveCharIP(character, ip)
Adds intelligence points to a character.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| character | <code>Character</code> | The character to damage. |
| ip | <code>Integer</code> | The amaount of intelligence points. |

<a name="getGadget"></a>

## getGadget(character, gadget) ⇒ <code>Gadget</code>
Returns specific gadget of a character.

**Kind**: global function
**Returns**: <code>Gadget</code> - The gadget if. (Can be null if not found).

| Param | Type | Description |
| --- | --- | --- |
| character | <code>Character</code> | The character. |
| gadget | <code>gadgetEnum</code> | The gadget type. |

<a name="removeGadget"></a>

## removeGadget(character, type)
Removes a specific gadget from a character.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| character | <code>Character</code> | The character to damage. |
| type | <code>gadgetEnum</code> | The gadget type. |

<a name="removeProperty"></a>

## removeProperty(character, type)
Removes a specific property from a character.

**Kind**: global function

| Param | Type | Description |
| --- | --- | --- |
| character | <code>Character</code> | The character to damage. |
| type | <code>propertyEnum</code> | The property type. |

<a name="getDistance"></a>

## getDistance(pos1, pos2) ⇒ <code>Integer</code>
Returns the distance of to points.

**Kind**: global function
**Returns**: <code>Integer</code> - The distance of those two points.

| Param | Type | Description |
| --- | --- | --- |
| pos1 | <code>Point</code> | The first point. |
| pos2 | <code>Point</code> | The second point. |

<a name="getNearestCharacter"></a>

## getNearestCharacter(coordinates, maxDist) ⇒ <code>Character</code>
Returns the nearest character to a specific point.

**Kind**: global function
**Returns**: <code>Character</code> - The nearest character.

| Param | Type | Description |
| --- | --- | --- |
| coordinates | <code>Point</code> | The point. |
| maxDist | <code>Integer</code> | The maximum distance. |

<a name="isNeighbor"></a>

## isNeighbor(position, target, [range]) ⇒ <code>Boolean</code>
Returns if one point is a neighbor of the other.

**Kind**: global function
**Returns**: <code>Boolean</code> - If they are neighbors.

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| position | <code>Point</code> |  | The position. |
| target | <code>Point</code> |  | The target position. |
| [range] | <code>Integer</code> | <code>1</code> | The range of neighborhood. |

<a name="getNeighbors"></a>

## getNeighbors(coordinates, [range]) ⇒ <code>Array.&lt;Point&gt;</code>
Returns all neighbors for a specific point.

**Kind**: global function
**Returns**: <code>Array.&lt;Point&gt;</code> - Array of neighbors.

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| coordinates | <code>Point</code> |  | The position. |
| [range] | <code>Integer</code> | <code>1</code> | The range of neighborhood. |

<a name="getRandomFreeNeighbor"></a>

## getRandomFreeNeighbor(coordinates, [range]) ⇒ <code>Point</code>
Returns a random free neighbor for a specific point.

**Kind**: global function
**Returns**: <code>Point</code> - Random free neighbor.

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| coordinates | <code>Point</code> |  | The position. |
| [range] | <code>Integer</code> | <code>1</code> | The range of neighborhood. |

<a name="getRandomNeighborCharacter"></a>

## getRandomNeighborCharacter(coordinates, [range], [sight]) ⇒ <code>Character</code>
Returns a random neighbor character for a specific point.

**Kind**: global function
**Returns**: <code>Character</code> - Random neighbor character.

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| coordinates | <code>Point</code> |  | The position. |
| [range] | <code>Integer</code> | <code>1</code> | The range of neighborhood. |
| [sight] | <code>Boolean</code> | <code>false</code> | If character has to be in sight (important for range > 1). |

<a name="getRandomCharacter"></a>

## getRandomCharacter() ⇒ <code>Character</code>
Returns a random character on the map.

**Kind**: global function
**Returns**: <code>Character</code> - Random character.
<a name="isInSight"></a>

## isInSight(position, target, chars) ⇒ <code>Boolean</code>
Return if two points are in sight.

**Kind**: global function
**Returns**: <code>Boolean</code> - If in sight.

| Param | Type | Description |
| --- | --- | --- |
| position | <code>Point</code> | The position. |
| target | <code>Point</code> | The target position. |
| chars | <code>Boolean</code> | If character block the sight. |

<a name="isCoordinatesValid"></a>

## isCoordinatesValid(coordinates) ⇒ <code>Boolean</code>
If point is on the map.

**Kind**: global function
**Returns**: <code>Boolean</code> - If is on map.

| Param | Type | Description |
| --- | --- | --- |
| coordinates | <code>Point</code> | The coordinates. |

<a name="doOperation"></a>

## doOperation(player, operation) ⇒ <code>Boolean</code>
Perfoms actions which a character can do with certain properties.

**Kind**: global function
**Returns**: <code>Boolean</code> - If operation was valid.

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| operation | <code>Operation</code> | The operation. |


* [doOperation(player, operation)](#doOperation) ⇒ <code>Boolean</code>
    * [~doPropertyAction(player, char, target, targetField, targetChar, usedProperty, operation)](#doOperation..doPropertyAction) ⇒ <code>Boolean</code>
    * [~doSpyAction(player, char, target, targetChar, operation)](#doOperation..doSpyAction) ⇒ <code>Boolean</code>
    * [~doMovementAction(player, target, char, targetChar, operation)](#doOperation..doMovementAction) ⇒ <code>Boolean</code>
    * [~doGambleAction(player, target, char, stake, targetField, operation)](#doOperation..doGambleAction) ⇒ <code>Boolean</code>
    * [~doGadgetAction(player, char, target, targetChar, operation, targetField, operation)](#doOperation..doGadgetAction) ⇒ <code>Boolean</code>
    * [~validizeTargetChar()](#doOperation..validizeTargetChar) ⇒
    * [~validizeGadget(gadget)](#doOperation..validizeGadget) ⇒
    * [~validizeDistance(target, maxDist)](#doOperation..validizeDistance) ⇒
    * [~validizeSight(target, chars)](#doOperation..validizeSight) ⇒
    * [~validizeFieldState(fieldState)](#doOperation..validizeFieldState) ⇒
    * [~validizeFieldGadget(gadget)](#doOperation..validizeFieldGadget) ⇒
    * [~validizeNeighbor(target, [range])](#doOperation..validizeNeighbor) ⇒
    * [~validizeStake(stake)](#doOperation..validizeStake) ⇒

<a name="doOperation..doPropertyAction"></a>

### doOperation~doPropertyAction(player, char, target, targetField, targetChar, usedProperty, operation) ⇒ <code>Boolean</code>
Performs property action which a character can do.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: <code>Boolean</code> - If action was valid.

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| char | <code>Character</code> | The player's character. |
| target | <code>Point</code> | The target. |
| targetField | <code>fieldStateEnum</code> | The target field. |
| targetChar | <code>Character</code> | The taget character (if on target field). |
| usedProperty | <code>propertyEnum</code> | The used property. |
| operation | <code>operationEnum</code> | The operation. |

<a name="doOperation..doSpyAction"></a>

### doOperation~doSpyAction(player, char, target, targetChar, operation) ⇒ <code>Boolean</code>
Perfoms spy on NPCs or on a safe.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: <code>Boolean</code> - If action was valid.

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| char | <code>Character</code> | The player's character. |
| target | <code>Point</code> | The target |
| targetChar | <code>Character</code> | The taget character (if on target field). |
| operation | <code>operationEnum</code> | The operation. |

<a name="doOperation..doMovementAction"></a>

### doOperation~doMovementAction(player, target, char, targetChar, operation) ⇒ <code>Boolean</code>
Perform move to a specific character.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: <code>Boolean</code> - If action was valid.

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| target | <code>Point</code> | The target. |
| char | <code>Character</code> | The player's character. |
| targetChar | <code>Character</code> | The taget character. (if on target field) |
| operation | <code>operationEnum</code> | The operation. |

<a name="doOperation..doGambleAction"></a>

### doOperation~doGambleAction(player, target, char, stake, targetField, operation) ⇒ <code>Boolean</code>
Perfroms roulette/gamble action.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: <code>Boolean</code> - If action was valid.

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| target | <code>Point</code> | The target. |
| char | <code>Character</code> | The player's character. |
| stake | <code>operationEnum</code> | The amount of stake. |
| targetField | <code>fieldStateEnum</code> | The target field. |
| operation | <code>operationEnum</code> | The operation. |

<a name="doOperation..doGadgetAction"></a>

### doOperation~doGadgetAction(player, char, target, targetChar, operation, targetField, operation) ⇒ <code>Boolean</code>
Performs gadget action for a specfic character.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: <code>Boolean</code> - If action was valid.

| Param | Type | Description |
| --- | --- | --- |
| player | <code>Player</code> | The player. |
| char | <code>Character</code> | The player's character. |
| target | <code>Point</code> | The target. |
| targetChar | <code>Character</code> | The taget character. (if on target field) |
| operation | <code>operationEnum</code> | The operation. |
| targetField | <code>fieldStateEnum</code> | The target field. |
| operation | <code>operationEnum</code> | The operation. |

<a name="doOperation..validizeTargetChar"></a>

### doOperation~validizeTargetChar() ⇒
Validizies targetChar

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: If valid.
<a name="doOperation..validizeGadget"></a>

### doOperation~validizeGadget(gadget) ⇒
Validizies gadget

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: If valid.

| Param | Type | Description |
| --- | --- | --- |
| gadget | <code>gadgetEnum</code> | The gadget. |

<a name="doOperation..validizeDistance"></a>

### doOperation~validizeDistance(target, maxDist) ⇒
Validizes distance between current position and target.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: If valid.

| Param | Type | Description |
| --- | --- | --- |
| target | <code>Point</code> | The target. |
| maxDist | <code>number</code> | The maximum distance. |

<a name="doOperation..validizeSight"></a>

### doOperation~validizeSight(target, chars) ⇒
Validizes sight between current position and target.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: If valid.

| Param | Type | Description |
| --- | --- | --- |
| target | <code>Point</code> | The target. |
| chars | <code>Boolean</code> | If characters block the sight. |

<a name="doOperation..validizeFieldState"></a>

### doOperation~validizeFieldState(fieldState) ⇒
Validizes if field is in specific state.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: If valid.

| Param | Type | Description |
| --- | --- | --- |
| fieldState | <code>fieldStateEnum</code> | The field state. |

<a name="doOperation..validizeFieldGadget"></a>

### doOperation~validizeFieldGadget(gadget) ⇒
Validizes if gadget is of specific type.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: Is valid.

| Param | Type | Description |
| --- | --- | --- |
| gadget | <code>gadgetEnum</code> | The gadget. |

<a name="doOperation..validizeNeighbor"></a>

### doOperation~validizeNeighbor(target, [range]) ⇒
Validizes if target is neighbor of current position.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: Is valid.

| Param | Type | Default | Description |
| --- | --- | --- | --- |
| target | <code>Point</code> |  | The target. |
| [range] | <code>Integer</code> | <code>1</code> | The range. |

<a name="doOperation..validizeStake"></a>

### doOperation~validizeStake(stake) ⇒
Validize stake.

**Kind**: inner method of [<code>doOperation</code>](#doOperation)
**Returns**: Is valid.

| Param | Type | Description |
| --- | --- | --- |
| stake | <code>Integer</code> | The stake. |