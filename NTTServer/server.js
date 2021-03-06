const uuidv4 = require("uuid").v4;
const WebSocket = require("ws");
const pauseable = require("pauseable");

const argv = require("yargs") // eslint-disable-line
  .command(
    "server[team]",
    "starts the server with team name",
    (yargs) => {
      yargs.positional("team", {
        describe: "Team name",
        default: 5000,
      });
    },
    (argv) => {
      if (argv.verbose) console.info(`start server of team ${argv.team}`);
    }
  )
  .demandOption(["config-charset", "config-match", "config-scenario", "x"])
  .option("config-charset", {
    alias: "c",
    type: "path",
    description: "Path to characters.json",
  })
  .option("config-match", {
    alias: "m",
    type: "path",
    description: "Path to matchconfig.match",
  })
  .option("config-scenario", {
    alias: "s",
    type: "path",
    description: "Path to scenarioconfig.scenario",
  })
  .option("port", {
    alias: "p",
    type: "port",
    description: "Run server on specific port",
  })
  .option("settings", {
    alias: "x",
    type: "object",
    description: "Optional settings",
  })
  .option("verbose", {
    alias: "v",
    type: "boolean",
    description: "Run with verbose logging",
  })
  .help("h")
  .alias("h", "help").argv;

var dateFormat = require("dateformat");
var Validator = require("jsonschema").Validator;
var v = new Validator();

const {
  messageTypeEnum,
  roleEnum,
  propertyEnum,
  gadgetEnum,
  operationEnum,
  errorEnum,
  victoryEnum,
  fieldStateEnum,
  genderEnum,
  phaseEnum,
  metaEnum,
} = require("./enums");

const characters = require(argv.c);
const matchconfig = require(argv.m);
const { scenario } = require(argv.s);
const settings = JSON.parse(argv.x);

const wss = new WebSocket.Server({ port: argv.port ? argv.port : 7007 });

const PHASE_TIMEOUT = matchconfig.turnPhaseLimit * 1000;
const RECONNECT_TIMEOUT = matchconfig.reconnectLimit * 1000;
const PAUSE_TIMEOUT = matchconfig.pauseLimit * 1000;

var messageSchema = {
  id: "/message",
  type: "object",
  properties: {
    clientId: {
      type: "string",
      format:
        "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
    },
    type: { type: "string", enum: Object.values(messageTypeEnum) },
    creationDate: {
      type: "string",
      format:
        "^([1-9]|([012][0-9])|(3[01]))-([0]{0,1}[1-9]|1[012])-dddd (20|21|22|23|[0-1]?d):[0-5]?d:[0-5]?d$",
    },
    debugMessage: { type: "string" },
  },
};
v.addSchema(messageSchema, "/message");

var helloSchema = {
  id: "/message/hello",
  allOf: [{ $ref: "/message" }],
  properties: {
    name: { type: "string" },
    role: { type: "string", enum: Object.values(roleEnum) },
  },
};

var reconnectSchema = {
  id: "/message/reconnect",
  allOf: [{ $ref: "/message" }],
  properties: {
    sessionId: {
      type: "string",
      format:
        "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
    },
  },
};

var itemChoiceSchema = {
  id: "/message/itemChoice",
  allOf: [{ $ref: "/message" }],
  properties: {
    chosenCharacterId: {
      type: "string",
      format:
        "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
    },
    chosenGadget: { type: "string", enum: Object.values(gadgetEnum) },
  },
};

var equipChoiceSchema = {
  id: "/message/equipChoice",
  allOf: [{ $ref: "/message" }],
  properties: {
    chosenCharacterIds: {
      type: "array",
      items: {
        type: "string",
        format:
          "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
      },
    },
    chosenGadgets: {
      type: "array",
      items: {
        type: "string",
        enum: Object.values(gadgetEnum),
      },
    },
  },
};

var gameOperationSchema = {
  id: "/message/gameOperation",
  allOf: [{ $ref: "/message" }],
  properties: {
    operation: {
      type: "object",
      properties: {
        type: {
          type: "string",
          enum: Object.values(operationEnum),
        },
        target: {
          type: "obejct",
          properties: {
            x: { type: "number" },
            y: { type: "number" },
          },
        },
        characterId: {
          type: "string",
          format:
            "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$",
        },
      },
    },
  },
};

var gameLeaveSchema = {
  id: "/message/gameLeave",
  allOf: [{ $ref: "/message" }],
  properties: {},
};

var metaSchema = {
  id: "/message/metaSchema",
  allOf: [{ $ref: "/message" }],
  properties: {
    keys: {
      type: "string",
      enum: Object.values(metaEnum),
    },
  },
};

var replaySchema = {
  id: "/message/replaySchema",
  allOf: [{ $ref: "/message" }],
  properties: {},
};

var requestGamePauseSchema = {
  id: "/message/requestGamePause",
  allOf: [{ $ref: "/message" }],
  properties: {
    gamePause: { type: "boolean" },
  },
};

var sessionId = uuidv4();

// assign id to each character
var characterSettings = [...characters];
var charactersObject = {};
characterSettings.forEach((char) => {
  char.characterId = uuidv4();
  charactersObject[char.characterId] = char;
});

// phase
var currentPhase = phaseEnum.INIT;
var choosableCharacterIds = characterSettings.map((char) => char.characterId);
var choosableGadgets = Object.values(gadgetEnum).filter(
  (gadget) =>
    gadget != gadgetEnum.DIAMOND_COLLAR && gadget != gadgetEnum.COCKTAIL
);

// game
var playerOne = {};
var playerTwo = {};
var statistics = [];
var hasReplay = true;
var gameStart = 0,
  gameEnd = 0;
var gamePaused = false;
var gameTimeout, itemTimeout, equipTimeout, pauseTimeout;

wss.on("connection", (ws) => {
  if (argv.verbose) console.info("new connection");

  /**
   * Closes the connection to a player and lets the other win.
   * @param {Websocket} ws The websocket client of a player 
   */
  disqualifyPlayer = (ws) => {
    ws.close();
    sendWinnerMessage(
      getOtherPlayer(ws.player).id,
      victoryEnum.VICTORY_BY_KICK
    );
  };

  ws.messages = [];

  ws.on("message", (message) => {
    const jsonMessage = JSON.parse(message);
    if (argv.verbose) console.info(jsonMessage);

    if (v.validate(jsonMessage, messageSchema)) {
      const { clientId, type, creationDate, debugMessage } = jsonMessage;
      var player = ws.player;
      var spectator = ws.spectator;

      switch (type) {
        case messageTypeEnum.HELLO: {
          if (v.validate(jsonMessage, helloSchema)) {
            const { name, role } = jsonMessage;
            console.log(role)
            if (role == roleEnum.PLAYER) {
              if (playerOne.connected && playerTwo.connected) {
                sendMessage(
                  ws,
                  messageTypeEnum.ERROR,
                  { reason: errorEnum.GENERAL },
                  "Server is full"
                );
                ws.close();
              } else {
                // player connects
                player = {
                  id: uuidv4(),
                  name,
                  role,
                  sendMessage: (type, data) => sendMessage(ws, type, data),
                  disqualifyPlayer: () => disqualifyPlayer(ws),
                  characters: [],
                  gadgets: [],
                  connected: true,
                  cocktrailDrinks: 0,
                  cocktailSpills: 0,
                  totalDamage: 0,
                };
                ws.player = player;

                playerOne.connected
                  ? (playerTwo = player)
                  : (playerOne = player);

                sendMessage(ws, messageTypeEnum.HELLO_REPLY, {
                  sessionId,
                  level: scenario,
                  settings: matchconfig,
                  characterSettings,
                });

                if (playerOne.connected && playerTwo.connected) {
                  if (argv.verbose) console.info("game start");
                  startGame();
                }
              }
            } else if (role == roleEnum.SPECTATOR) {
              spectator = {
                id: uuidv4(),
                name,
                role,
                sendMessage: (type, data) => sendMessage(ws, type, data),
              };
              ws.spectator = spectator;

              sendMessage(ws, messageTypeEnum.HELLO_REPLY, {
                sessionId,
                level: scenario,
                settings: matchconfig,
                characterSettings,
              });
            }
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.RECONNECT: {
          if (player && v.validate(jsonMessage, reconnectSchema)) {
            if (jsonMessage.sessionId == sessionId) {
              player = playerOne.connected ? playerTwo : playerOne;

              gamePaused = false;

              sendMessage(ws, messageTypeEnum.HELLO_REPLY, {
                sessionId,
                level: scenario,
                settings: matchconfig,
                characterSettings,
              });

              // send phase data
              switch (currentPhase) {
                case phaseEnum.ITEM_CHOSE: {
                  sendMessage(ws, messageTypeEnum.REQUEST_ITEM_CHOICE, {
                    offeredCharacterIds: getOfferedCharacterIds(player),
                    offeredGadgets: getOfferedGadgets(player),
                  });
                  break;
                }
                case phaseEnum.EQUIP_CHOSE: {
                  sendMessage(ws, messageTypeEnum.REQUEST_EQUIPMENT_CHOICE, {
                    chosenCharacterIds: player.characters,
                    chosenGadgets: player.gadgets,
                  });
                  break;
                }
              }
            } else {
              // Occurs when an attempt is made to access a session that does not exist using a ReconnectMessage or otherwise
              sendMessage(ws, messageTypeEnum.ERROR, {
                reason: errorEnum.SESSION_DOES_NOT_EXIST,
                debugMessage: "Incorrect session id",
              });
            }
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.ITEM_CHOICE: {
          if (player && v.validate(jsonMessage, itemChoiceSchema)) {
            if (currentPhase === phaseEnum.ITEM_CHOSE) {
              const { chosenCharacterId, chosenGadget } = jsonMessage;
              if (argv.verbose) console.info(chosenCharacterId, chosenGadget);

              if (chosenCharacterId ? chosenGadget : !chosenGadget) {
                // logical XNOR -> cant be both null or chosen
                sendMessage(
                  ws,
                  messageTypeEnum.ERROR,
                  { reason: errorEnum.ILLEGAL_MESSAGE },
                  "Either chosenCharacterId or chosenGadget has to be null"
                );
                return;
              }

              if (chosenCharacterId) {
                if (!player.offeredCharacterIds.includes(chosenCharacterId)) {
                  sendMessage(
                    ws,
                    messageTypeEnum.ERROR,
                    { reason: errorEnum.ILLEGAL_MESSAGE },
                    chosenCharacterId + " was not offered"
                  );
                  return;
                }
                player.characters.push(chosenCharacterId); // add to player
              } else if (chosenGadget) {
                if (!player.offeredGadgets.includes(chosenGadget)) {
                  sendMessage(
                    ws,
                    messageTypeEnum.ERROR,
                    { reason: errorEnum.ILLEGAL_MESSAGE },
                    chosenGadget + " was not offered"
                  );
                  return;
                }
                player.gadgets.push(chosenGadget); // add to player
              }

              choosableCharacterIds.push(
                ...player.offeredCharacterIds.filter(
                  (id) => id !== chosenCharacterId
                )
              ); // lay back the rest
              choosableGadgets.push(
                ...player.offeredGadgets.filter((id) => id !== chosenGadget)
              ); // lay back the rest

              // if slots are filled
              if (player.characters.length + player.gadgets.length < 8) {
                sendMessage(ws, messageTypeEnum.REQUEST_ITEM_CHOICE, {
                  offeredCharacterIds: getOfferedCharacterIds(player),
                  offeredGadgets: getOfferedGadgets(player),
                });
              }
            } else {
              sendMessage(ws, messageTypeEnum.ERROR, {
                reason: errorEnum.ILLEGAL_MESSAGE,
                debugMessage: "Incorrect phase",
              });
            }
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.EQUIPMENT_CHOICE: {
          if (player && v.validate(jsonMessage, equipChoiceSchema)) {
            if (currentPhase === phaseEnum.EQUIP_CHOSE) {
              var equipment = jsonMessage.equipment;

              var isValid = true; // check if uuids and gadgets are in player pool
              for (var uuid in equipment) {
                if (!player.characters.includes(uuid)) isValid = false;
                for (var gadget of equipment[uuid]) {
                  if (!player.gadgets.includes(gadget)) isValid = false;
                }
              }

              if (isValid) {
                player.equipment = equipment; //set equipment
              } else {
                // close connection if not valid
                player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
                  reason: errorEnum.ILLEGAL_MESSAGE,
                  debugMessage: "Equipment is not valid",
                });
                player.disqualifyPlayer();
              }
            } else {
              sendMessage(ws, messageTypeEnum.ERROR, {
                reason: errorEnum.ILLEGAL_MESSAGE,
                debugMessage: "Incorrect phase",
              });
            }
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.GAME_OPERATION: {
          if (player && v.validate(jsonMessage, gameOperationSchema)) {
            if (
              currentPhase === phaseEnum.GAME &&
              player.activeCharacterId === activeCharacterId
            ) {
              var operation = jsonMessage.operation;

              if (
                operation.characterId == activeCharacterId &&
                (operation.type == operationEnum.RETIRE ||
                  doOperation(player, operation))
              ) {
                // Siegesbedingung überprüfen
                if (!isGameOver) {
                  // Send game status message
                  sendGameStatusMessage();

                  // Next turn
                  if (
                    (gameCharacters[activeCharacterId].mp > 0 ||
                      gameCharacters[activeCharacterId].ap > 0) &&
                    operation.type != operationEnum.RETIRE
                  ) {
                    player.sendMessage(
                      // request new operation from active character
                      messageTypeEnum.REQUEST_GAME_OPERATION,
                      {
                        characterId: activeCharacterId,
                      },
                      "Character turn"
                    );
                  } else {
                    nextTurn();
                  }
                } else {
                  // Send game status message
                  sendGameStatusMessage();
                  determineWinner();
                }
              }
            } else {
              sendMessage(ws, messageTypeEnum.ERROR, {
                reason: errorEnum.ILLEGAL_MESSAGE,
                debugMessage: "Incorrect phase",
              });
            }
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.GAME_LEAVE: {
          if (v.validate(jsonMessage, gameLeaveSchema)) {
            ws.close();
            if (player) {
              broadcastMessage(messageTypeEnum.GAME_LEFT, {
                leftUserId: player.id,
              });
              sendWinnerMessage(
                getOtherPlayer(ws.player).id,
                victoryEnum.VICTORY_BY_LEAVE
              );
            } else if (spectator) {
              spectator.sendMessage({
                leftUserId: spectator.id,
              });
            }
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.REQUEST_GAME_PAUSE: {
          if (player && v.validate(jsonMessage, requestGamePauseSchema)) {
            if (jsonMessage.gamePause) {
              pauseGame(player);
            } else {
              resumeGame();
            }

            broadcastMessage(messageTypeEnum.GAME_PAUSE, {
              gamePaused,
              serverEnforced: false,
            });
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.REQUEST_META_INFORMATION: {
          if (v.validate(jsonMessage, metaSchema)) {
            const { keys } = jsonMessage;
            var information = {};

            if (keys.includes(metaEnum.CONFIG_SCENARIO)) {
              information[metaEnum.CONFIG_SCENARIO] = scenario;
            }
            if (keys.includes(metaEnum.CONFIG_MATCH)) {
              information[metaEnum.CONFIG_MATCH] = matchconfig;
            }
            if (keys.includes(metaEnum.CONFIG_CHAR)) {
              information[metaEnum.CONFIG_CHAR] = characters;
            }
            if (keys.includes(metaEnum.FRAC_ONE)) {
              information[metaEnum.FRAC_ONE] =
                player && player.id == playerTwo.id
                  ? null
                  : playerOne.characters;
            }
            if (keys.includes(metaEnum.FRAC_TWO)) {
              information[metaEnum.FRAC_TWO] =
                player && player.id == playerOne.id
                  ? null
                  : playerTwo.characters;
            }
            if (keys.includes(metaEnum.FRAC_NEUTRAL)) {
              information[metaEnum.FRAC_NEUTRAL] = player
                ? null
                : npcCharacters;
            }
            if (keys.includes(metaEnum.GAD_ONE)) {
              information[metaEnum.GAD_ONE] =
                player && player.id == playerTwo.id ? null : playerOne.gadgets;
            }
            if (keys.includes(metaEnum.GAD_TWO)) {
              information[metaEnum.GAD_TWO] =
                player && player.id == playerOne.id ? null : playerTwo.gadgets;
            }

            sendMessage(ws, messageTypeEnum.META_INFORMATION, {
              information,
            });
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
        case messageTypeEnum.REQUEST_REPLAY: {
          if (v.validate(jsonMessage, replaySchema)) {
            sendMessage(ws, messageTypeEnum.REPLAY, {
              sessionId,
              gameStart,
              gameEnd,
              playerOneId: playerOne.id,
              playerTwoId: playerTwo.id,
              playerOneName: playerOne.name,
              playerTwoName: playerTwo.name,
              rounds: currentRound,
              level: scenario,
              settings: matchconfig,
              characterSettings: characters,
              messages: ws.messages,
            });
          } else {
            sendMessage(ws, messageTypeEnum.ERROR, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Incorrect schema",
            });
          }
          break;
        }
      }
    } else {
      sendMessage(ws, messageTypeEnum.ERROR, {
        reason: errorEnum.ILLEGAL_MESSAGE,
      });
    }
  });

  ws.on("close", (number, reason) => {
    var player = ws.player;
    if (player) {
      player.connected = false;
      if (argv.verbose)
        console.info(
          playerOne.connected
            ? "playerTwo disconnected"
            : "playerOne disconnected",
          number,
          reason
        );

      pauseGame(player);

      broadcastMessage(messageTypeEnum.GAME_PAUSE, {
        gamePaused,
        serverEnforced: true,
      });

      setTimeout(() => {
        if (!player.connected) {
          broadcastMessage(messageTypeEnum.STATISTICS, {
            statistics,
            winner: getOtherPlayer(player).id,
            reason: victoryEnum.VICTORY_BY_LEAVE,
            hasReplay,
          });
        }
      }, RECONNECT_TIMEOUT);
    }
  });

  /**
   * Determines the winner.
   */
  determineWinner = () => {
    var playerOneIPs = 0;
    for (let characterId of playerOne.characters) {
      playerOneIPs += gameCharacters[characterId].ip;
      playerOneIPs +=
        gameCharacters[characterId].chips * matchconfig.chipsToIpFactor;
    }
    var playerTwoIPs = 0;
    for (let characterId of playerTwo.characters) {
      playerTwoIPs += gameCharacters[characterId].ip;
      playerTwoIPs +=
        gameCharacters[characterId].chips * matchconfig.chipsToIpFactor;
    }

    if (playerOneIPs > playerTwoIPs) {
      sendWinnerMessage(playerOne.id, victoryEnum.VICTORY_BY_IP);
    } else if (playerOneIPs < playerTwoIPs) {
      sendWinnerMessage(playerTwo.id, victoryEnum.VICTORY_BY_IP);
    }

    if(catHandover) {
      if (catHandover.id == playerOne.id) {
        sendWinnerMessage(playerOne.id, victoryEnum.VICTORY_BY_COLLAR);
      } else if (catHandover.id == playerTwo.id) {
        sendWinnerMessage(playerTwo.id, victoryEnum.VICTORY_BY_COLLAR);
      }
    }

    if (playerOne.cocktrailDrinks > playerTwo.cocktrailDrinks) {
      sendWinnerMessage(playerOne.id, victoryEnum.VICTORY_BY_DRINKING);
    } else if (playerOne.cocktrailDrinks < playerTwo.cocktrailDrinks) {
      sendWinnerMessage(playerTwo.id, victoryEnum.VICTORY_BY_DRINKING);
    }

    if (playerOne.cocktailSpills > playerTwo.cocktailSpills) {
      sendWinnerMessage(playerOne.id, victoryEnum.VICTORY_BY_SPILLING);
    } else if (playerOne.cocktailSpills < playerTwo.cocktailSpills) {
      sendWinnerMessage(playerTwo.id, victoryEnum.VICTORY_BY_SPILLING);
    }

    if (playerOne.totalDamage > playerTwo.totalDamage) {
      sendWinnerMessage(playerTwo.id, victoryEnum.VICTORY_BY_HP);
    } else if (playerOne.totalDamage < playerTwo.totalDamage) {
      sendWinnerMessage(playerOne.id, victoryEnum.VICTORY_BY_HP);
    }

    if (Math.round(Math.random())) {
      sendWinnerMessage(playerOne.id, victoryEnum.VICTORY_BY_RANDOMNESS);
    } else {
      sendWinnerMessage(playerTwo.id, victoryEnum.VICTORY_BY_RANDOMNESS);
    }
  };

  /**
   * Broadcasts the winner message.
   * @param {String} playerId The player id of the winner.
   * @param {messageTypeEnum} type The win reason.
   */
  sendWinnerMessage = (playerId, type) => {
    broadcastMessage(messageTypeEnum.STATISTICS, {
      statistics,
      winner: playerId,
      reason: type,
      hasReplay,
    });
  };

  /**
   * Returns the other player.
   * @param {Player} player The player.
   * @return {Player} The player's opponent.
   */
  getOtherPlayer = (player) => {
    return player.id == playerOne.id ? playerTwo : playerOne;
  };

  /**
   * Returns a random player.
   * @return {Player} Either playerOne oder playerTwo.
   */
  getRandomPlayer = () => {
    return Math.round(Math.random()) ? playerOne : playerTwo;
  };

  /**
   * Returns the offered character ids for a player.
   * @param {Player} player The player.
   * @return {String[]} Array of offered character ids.
   */
  getOfferedCharacterIds = (player) => {
    var offeredCharacterIds = [];
    var num = player.characters.length == 4 ? 0 : 3;
    for (var i = 0; i < num; i++) {
      let r = Math.round(Math.random() * (choosableCharacterIds.length - 1));
      offeredCharacterIds.push(choosableCharacterIds[r]);
      choosableCharacterIds.splice(r, 1);
    }
    player.offeredCharacterIds = offeredCharacterIds;
    return offeredCharacterIds;
  };

  /**
   * Returns the offered gadgets.
   * @param {Player} player The player.
   * @return {String[]} Array of offered gadgets.
   */
  getOfferedGadgets = (player) => {
    var offeredGadgets = [];
    var num = player.gadgets.length == 6 ? 0 : 3;
    for (var i = 0; i < num; i++) {
      let r = Math.round(Math.random() * (choosableGadgets.length - 1));
      offeredGadgets.push(choosableGadgets[r]);
      choosableGadgets.splice(r, 1);
    }
    player.offeredGadgets = offeredGadgets;
    return offeredGadgets;
  };
});

/**
 * Sends a message to a websocket client.
 * @param {Websocket} ws The receiving websocket client.
 * @param {messageTypeEnum} type The type of the message.
 * @param {Object} data The data to send.
 * @param {String} debugMessage The debug message to send.
 */
sendMessage = (ws, type, data, debugMessage = "") => {
  var message = JSON.stringify({
    clientId: ws.player ? ws.player.id : undefined,
    type,
    creationDate: dateFormat(new Date(), "dd.mm.yyyy hh:MM:ss"),
    debugMessage,
    ...data,
  });
  ws.messages.push(message);
  ws.send(message);
  if (argv.verbose) console.info("sending", message);
};

/**
 * Broadcasts a message to a all websocket clients.
 * @param {messageTypeEnum} type The type of the message.
 * @param {Object} data The data to send.
 * @param {String} debugMessage The debug message to send.
 */
broadcastMessage = (type, data, debugMessage = "") => {
  wss.clients.forEach((ws) => sendMessage(ws, type, data, debugMessage));
};

/**
 * Ends the game.
 */
endGame = () => {
  endGame = dateFormat(new Date(), "dd.mm.yyyy hh:MM:ss");
  isGameOver = true;
};

/**
 * Pauses the game.
 */
pauseGame = (player) => {
  gamePaused = true;
  if(itemTimeout) itemTimeout.pause();
  if(equipTimeout) equipTimeout.pause();
  if(gameTimeout) gameTimeout.pause();

  pauseTimeout = pauseable.setTimeout(() => {
    broadcastMessage(messageTypeEnum.STATISTICS, {
      statistics,
      winner: getOtherPlayer(player).id,
      reason: victoryEnum.VICTORY_BY_LEAVE,
      hasReplay,
    });
  }, PAUSE_TIMEOUT);
};

/**
 * Resumes the game.
 */
resumeGame = () => {
  gamePaused = false;
  if(itemTimeout) itemTimeout.resume();
  if(equipTimeout) equipTimeout.resume();
  if(gameTimeout) gameTimeout.resume();

  if(pauseTimeout) pauseTimeout.clear();
};

/**
 * Sarts the game.
 */
startGame = () => {
  gameStart = dateFormat(new Date(), "dd.mm.yyyy hh:MM:ss");

  broadcastMessage(messageTypeEnum.GAME_STARTED, {
    playerOneId: playerOne.id,
    playerOneName: playerOne.name,
    playerTwoId: playerTwo.id,
    playerTwoName: playerTwo.name,
    sessionId,
  });

  // Start choosing phase
  if (argv.verbose) console.info("item chose phase");
  currentPhase = phaseEnum.ITEM_CHOSE;
  playerOne.sendMessage(messageTypeEnum.REQUEST_ITEM_CHOICE, {
    offeredCharacterIds: getOfferedCharacterIds(playerOne),
    offeredGadgets: getOfferedGadgets(playerOne),
  });
  playerTwo.sendMessage(messageTypeEnum.REQUEST_ITEM_CHOICE, {
    offeredCharacterIds: getOfferedCharacterIds(playerTwo),
    offeredGadgets: getOfferedGadgets(playerTwo),
  });

  itemTimeout = pauseable.setTimeout(() => {
    // Close connection if slots not filled
    if (playerOne.characters.length + playerOne.gadgets.length < 8) {
      playerOne.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
        reason: errorEnum.ILLEGAL_MESSAGE,
        debugMessage: "Not all slots were filled",
      });
      playerOne.disqualifyPlayer();
    }
    if (playerTwo.characters.length + playerTwo.gadgets.length < 8) {
      playerTwo.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
        reason: errorEnum.ILLEGAL_MESSAGE,
        debugMessage: "Not all slots were filled",
      });
      playerTwo.disqualifyPlayer();
    }

    // Move on to equip phase
    if (argv.verbose) console.info("equip chose phase");

    currentPhase = phaseEnum.EQUIP_CHOSE;
    playerOne.sendMessage(messageTypeEnum.REQUEST_EQUIPMENT_CHOICE, {
      chosenCharacterIds: playerOne.characters,
      chosenGadgets: playerOne.gadgets,
    });
    playerTwo.sendMessage(messageTypeEnum.REQUEST_EQUIPMENT_CHOICE, {
      chosenCharacterIds: playerTwo.characters,
      chosenGadgets: playerTwo.gadgets,
    });

    equipTimeout = pauseable.setTimeout(() => {
      // Close connection if no equipment
      if (!playerOne.equipment) {
        playerOne.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "No equipment",
        });
        playerOne.disqualifyPlayer();
      }
      if (!playerTwo.equipment) {
        playerTwo.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "No equipment",
        });
        playerTwo.disqualifyPlayer();
      }

      // Move on to game phase
      if (argv.verbose) console.info("game phase");
      currentPhase = phaseEnum.GAME;

      // Set map
      var safeCount = 1;
      var safeIndices = [];
      var freeFields = [];
      for (let row in scenario) {
        var fields = [];
        for (let col in scenario[row]) {
          let state = scenario[row][col];
          fields.push({
            state,
            gadget:
              state == fieldStateEnum.BAR_TABLE
                ? { gadget: gadgetEnum.COCKTAIL, usages: 0, isPoisoned: false }
                : null, // place cocktails on bar tables
            isDestroyed: false,
            isInverted: false,
            chipAmount: matchconfig.maxChipsRoulette,
            safeIndex: null,
            isFoggy: false,
            isUpdated: true,
          });
          if (state == fieldStateEnum.SAFE) {
            safeIndices.push(safeCount);
            safeCount++;
          }
          if (
            state === fieldStateEnum.FREE ||
            state === fieldStateEnum.BAR_SEAT
          )
            freeFields.push({ x: parseInt(col), y: parseInt(row) }); // add to free fields
        }
        map.push(fields);
      }

      //randomize safe indices https://stackoverflow.com/questions/6274339/how-can-i-shuffle-an-array
      for (let i = safeIndices.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [safeIndices[i], safeIndices[j]] = [safeIndices[j], safeIndices[i]];
      }

      // set safe indices and combinations
      safeIndex = 0;
      for (let row in map) {
        for (let col in row) {
          let field = map[row][col];
          field.safeIndex = safeIndices[safeIndex];
          safeCombinations.push(field.safeIndex);
          // put collar in random safe
          if (safeIndex == 0) {
            field.gadget = {
              gadget: gadgetEnum.DIAMOND_COLLAR,
              usages: 0,
            };
          }
          safeIndex++;
        }
      }

      // Set gameCharacters
      for (let i = 0; i < npcCount; i++) {
        // add random NPCs
        if (choosableCharacterIds.length > 0) {
          let r = Math.round(
            Math.random() * (choosableCharacterIds.length - 1)
          );
          let randomFreeField = Math.round(
            Math.random() * (freeFields.length - 1)
          ); // get random free field

          let characterId = choosableCharacterIds[r];
          let { name, features, gender } = charactersObject[characterId];
          gameCharacters[characterId] = {
            characterId,
            name,
            gender,
            coordinates: freeFields[randomFreeField],
            properties: features,
            deactivatedProperties: [],
            mp: 2,
            ap: 1,
            hp: 100,
            ip: 0,
            chips: 10,
            gadgets: [],
          };
          freeFields.splice(randomFreeField, 1);
          npcCharacters.push(choosableCharacterIds[r]);
          choosableCharacterIds.splice(r, 1);
        }
      }

      setGameCharacters(playerOne, freeFields);
      setGameCharacters(playerTwo, freeFields);

      // Set cat on random free field
      let randomFreeField = Math.round(Math.random() * (freeFields.length - 1));
      catCoordinates = freeFields[randomFreeField];
      freeFields.splice(randomFreeField, 1);

      sendGameStatusMessage(); // send initial game status message
      startRound(getRandomPlayer()); //start first round with random player
    }, PHASE_TIMEOUT);
  }, PHASE_TIMEOUT);
};

/**
 * Initiates the characters of a player.
 * @param {Player} player The player.
 * @param {Field[]} freeFields The remaining free fields on the map.
 */
setGameCharacters = (player, freeFields) => {
  for (let characterId in player.equipment) {
    let { name, features, gender } = charactersObject[characterId];
    let charGadgets = player.equipment[characterId];
    let randomFreeField = Math.round(Math.random() * (freeFields.length - 1)); // get random free field
    let gadgets = [];
    for (let gadget of charGadgets) {
      if (gadget == gadgetEnum.WIRETAP_WITH_EARPLUGS) {
        gadgets.push({
          gadget,
          usages: 0,
          isActive: null,
          isWorking: true,
        });
      } else {
        gadgets.push({
          gadget,
          usages: 0,
        });
      }
    }
    gameCharacters[characterId] = {
      characterId,
      name,
      gender,
      coordinates: freeFields[randomFreeField],
      properties: features,
      deactivatedProperties: [],
      mp: 2,
      ap: 1,
      hp: 100,
      ip: 0,
      chips: 10,
      gadgets,
    };
    freeFields.splice(randomFreeField, 1); // remove free field
  }
};

var npcCount = settings.npcCount ? settings.npcCount : 3;
var npcCharacters = [];
var safeCombinations = [];
var currentRound = 0;
var map = [];
var gameCharacters = {};
var characterOrder = [];
var catCoordinates = null;
var janitorCoordinates = null;
var janitorID = uuidv4();
var catID = uuidv4();
var catHandover = null;
var activeCharacterId = null;
var wireTapChar = null;
var earPlugsChar = null;
var fogTinTime = 0;
var fogTin = null;
var isGameOver = false;
var latestOperations = [];

/**
 * Returns the current game state in respect to a specific client.
 * @param {Websocket} ws The websocket client.
 * @return {Object} The game state.
 */
getGameState = (ws) => {
  return {
    currentRound,
    map: {map},
    mySafeCombinations: ws.player ? ws.player.safeCombinations : [],
    characters: Object.values(gameCharacters),
    catCoordinates,
    janitorCoordinates,
  };
};

/**
 * Broadcasts the current game state.
 */
sendGameStatusMessage = () => {
  wss.clients.forEach((ws) => {
    sendMessage(
      ws,
      messageTypeEnum.GAME_STATUS,
      {
        activeCharacterId,
        operations: latestOperations,
        state: getGameState(ws),
        isGameOver,
      },
      "Game state"
    );
  });

  latestOperations = [];
};

/**
 * Starts a new round
 */
startRound = () => {
  currentRound++;

  // Janitor
  if (currentRound == matchconfig.roundLimit) {
    var freeFields = [];
    for (let row in map) {
      for (let col in row) {
        let field = map[row][col];
        if (
          field == fieldStateEnum.FREE &&
          !getCharacterByPosition({ x: col, y: row })
        ) {
          freeFields.push(field);
        }
      }
    }
    janitorCoordinates =
      freeFields[Math.round(Math.random() * (freeFields.length - 1))]; // spawn on random field

    npcCharacters.forEach((characterId) => {
      // remove all NPCs
      delete gameCharacters[characterId];
    });

    sendGameStatusMessage();
  }

  // Fill cocktails and do fireplace dry
  for (let row in map) {
    for (let col in row) {
      let field = map[row][col];
      if (field.state === fieldStateEnum.BAR_TABLE) {
        field.gadget = {
          gadget: gadgetEnum.COCKTAIL,
          usages: 0,
          isPoisoned: false,
        };
      } else if (field.state === fieldStateEnum.FIREPLACE) {
        getNeighbors({ x: col, y: row }).forEach((neighbor) => {
          let neighborChar = getCharacterByPosition(neighbor);
          if (
            neighborChar &&
            neighborChar.properties.includes(propertyEnum.CLAMMY_CLOTHES)
          ) {
            removeProperty(neighborChar, propertyEnum.CLAMMY_CLOTHES);
          }
        });
      }
    }
  }

  // Ausfallwahrscheinlichkeitsprobe and bar seat and anti-plague-mask
  for (let char of Object.values(gameCharacters)) {
    for (let gadget of char.gadgets) {
      if (gadget.gadget == gadgetEnum.WIRETAP_WITH_EARPLUGS) {
        if (getChance(char, matchconfig.wiretapWithEarplugsFailChance)) {
          gadget.isWorking = false;
          gadget.activeOn = null;
          wireTapChar = null;
          earPlugsChar = null;
        }
      }
      if (gadget.gadget == gadgetEnum.ANTI_PLAGUE_MASK) {
        char.hp = Math.min(100, char.hp + 10);
      }
    }

    if (
      map[char.coordinates.y][char.coordinates.x].state ===
      fieldStateEnum.BAR_SEAT
    ) {
      char.hp = 100;
    }
  }

  // Überprüfen ob Fog Tin aktiv
  if (fogTin) {
    fogTinTime++;
    if (fogTinTime == 3) {
      var fogTinField = map[fogTin.y][fogTin.x];
      fogTinField.isFoggy = false;
      fogTinIsActive = false;
      getNeighbors(fogTin).forEach((coords) => {
        let field = map[coords.y][coords.x];
        field.isFoggy = false;
      });
    }
  }

  // Zugreihenfolge der charaktere
  setCharacterOrder();

  if (currentRound >= matchconfig.roundLimit) {
    characterOrder.splice(
      Math.round(Math.random() * (characterOrder.length - 1)),
      0,
      janitorID
    ); // random turn place
  }
  characterOrder.splice(
    Math.round(Math.random() * (characterOrder.length - 1)),
    0,
    catID
  ); // random turn place

  // Erste Zugphase
  startTurn(characterOrder[0]);
};

/**
 * Sets the order of all characters.
 */
setCharacterOrder = () => {
  // https://stackoverflow.com/questions/6274339/how-can-i-shuffle-an-array
  var a = Object.keys(gameCharacters);
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  characterOrder = a;
};

/**
 * Starts a new turn for a specific character.
 * @param {String} characterId The id of the character.
 */
startTurn = (characterId) => {
  activeCharacterId = characterId;

  if (argv.verbose) console.info("Turn: ", characterId);

  if (npcCharacters.includes(activeCharacterId)) {
    // NPC turn
    if (argv.verbose) console.info("NPC turn");

    var targetChar = gameCharacters[characterId];
    while (targetChar.mp > 0 || targetChar.ap > 0) {
      if (argv.verbose) console.info("NPC loop", targetChar.mp, targetChar.ap);

      if (targetChar.mp > 0) {
        var freeNeighbors = [];
        getNeighbors(targetChar.coordinates).forEach((coords) => {
          let field = map[coords.y][coords.x];
          if (
            field.state == fieldStateEnum.FREE ||
            field.state == fieldStateEnum.BAR_SEAT
          )
            freeNeighbors.push(coords);
        });

        if(freeNeighbors.length > 0) {
          var target =
            freeNeighbors[Math.round(Math.random() * (freeNeighbors.length - 1))];

          if (argv.verbose) console.info("NPC moves to ", target);
          
          doOperation(null, {
            type: operationEnum.MOVEMENT,
            target,
            characterId,
          });
        } else {
          if (argv.verbose) console.info("NPC cannot move");
          targetChar.mp--;
        }
      }

      if (targetChar.ap > 0) {
        if (getGadget(targetChar, gadgetEnum.MOLEDIE)) {
          if (argv.verbose) console.info("NPC throws moledie ");

          doOperation(null, {
            type: operationEnum.GADGET_ACTION,
            target: getRandomFreeNeighbor(targetChar.coordinates),
            characterId,
          });
        } else {
          targetChar.ap--;
        }
      }
    }

    sendGameStatusMessage();
    nextTurn();
  } else if (characterId == janitorID) {
    // janitor turn
    if (argv.verbose) console.info("Janitor turn");

    var nearestChar = getNearestCharacter(janitorCoordinates);
    janitorCoordinates = nearestChar.coordinates; // jump to nearest character
    delete gameCharacters[nearestChar.characterId]; // remove character
    characterOrder.splice(characterOrder.indexOf(nearestChar.characterId), 1); // remove from turn order

    if (Object.keys(gameCharacters).length == 0) {
      endGame();
      sendGameStatusMessage();
      determineWinner();
    } else {
      sendGameStatusMessage();
      nextTurn();
    }
  } else if (characterId == catID) {
    // cat turn
    if (argv.verbose) console.info("Cat turn");

    var freeNeighbors = [];
    getNeighbors(catCoordinates).forEach((coords) => {
      let field = map[coords.y][coords.x];
      if (
        field.state == fieldStateEnum.FREE ||
        field.state == fieldStateEnum.BAR_SEAT
      )
        freeNeighbors.push(coords);
    });

    var target =
      freeNeighbors[Math.round(Math.random() * (freeNeighbors.length - 1))];
    var targetField = map[target.y][target.x];

    catCoordinates = target;

    if (
      targetField.gadget &&
      targetField.gadget.gadget == gadgetEnum.DIAMOND_COLLAR
    ) {
      endGame();
      sendGameStatusMessage();
      determineWinner();
    } else {
      sendGameStatusMessage();
      nextTurn();
    }
  } else {
    // player turn
    if (argv.verbose) console.info("Player turn");

    var player = playerOne.characters.includes(characterId)
      ? playerOne
      : playerTwo; // determine player

    player.activeCharacterId = characterId;
    prepareTurn(activeCharacterId);

    player.sendMessage(
      messageTypeEnum.REQUEST_GAME_OPERATION,
      {
        characterId: activeCharacterId,
      },
      "Character turn"
    );

    gameTimeout = pauseable.setTimeout(() => {
      if (activeCharacterId == characterId) {
        player.strikes++;
        player.sendMessage(messageTypeEnum.STRIKE, {
          strikeNr: player.strikes,
          strikeMax: matchconfig.strikeMaximum,
          reason: "Turn took to long",
        });
      }
      if (player.strikes == matchconfig.strikeMaximum) {
        player.disqualifyPlayer();
      }
    }, PHASE_TIMEOUT);
  }
};

/**
 * Starts the next turn or next round if all characters had their turn.
 */
nextTurn = () => {
  if (argv.verbose) console.info("Next turn");

  // Next round if last character
  if (activeCharacterId == characterOrder[characterOrder.length - 1]) {
    startRound();
  } else {
    // Next character
    var nextCharId =
      characterOrder[characterOrder.indexOf(activeCharacterId) + 1];
    startTurn(nextCharId);
  }
};

/**
 * Prepares a characters turn and sets their mps and aps in respect to their properties.
 * @param {String} characterId The id of the character.
 */
prepareTurn = (characterId) => {
  var character = gameCharacters[characterId];
  if(character) {
    var mp = 2,
      ap = 1;
    if (character.properties.includes(propertyEnum.NIMBLENESS)) mp++;
    if (character.properties.includes(propertyEnum.SLUGGISHNESS)) mp--;
    if (character.properties.includes(propertyEnum.SPRYNESS)) ap++;
    if (character.properties.includes(propertyEnum.PONDEROUSNESS)) {
      if (Math.round(Math.random())) mp--;
      else ap--;
    }
    if (character.properties.includes(propertyEnum.AGILITY)) {
      if (Math.round(Math.random())) mp++;
      else ap++;
    }
    character.mp = mp;
    character.ap = ap;
  }
};

/**
 * Returns a character (if on field) by position.
 * @param {Point} coordinates The position.
 * @return {Character} The character. (null if no character on that position)
 */
getCharacterByPosition = (coordinates) => {
  for (let char of Object.values(gameCharacters)) {
    if (
      char.coordinates.x == coordinates.x &&
      char.coordinates.y == coordinates.y
    ) {
      return char;
    }
  }
  return null;
};

/**
 * Returns true or false for a character with a specific chance in respect to their properties.
 * @param {Character} character The character.
 * @param {Double} chance The chance.
 * @return {Boolean} Random boolean.
 */
getChance = (character, chance) => {
  if (
    character.properties.includes(propertyEnum.CLAMMY_CLOTHES) ||
    character.properties.includes(propertyEnum.CONSTANT_CLAMMY_CLOTHES)
  ) {
    chance /= 2;
  }
  if (
    !(Math.random() <= chance) &&
    character.properties.includes(propertyEnum.TRADECRAFT)
  ) {
    return Math.random() <= chance;
  }
  return true;
};

/**
 * Adds damage to a character in respect to their properties. (Decreases hp)
 * @param {Character} character The character to damage.
 * @param {Integer} damage The amaount of damage.
 * @param {Boolean} poison If cause of damage is a poisoned cocktail.
 */
damageCharacter = (character, damage, poison = false) => {
  var successful = true;
  if (character.properties.includes(propertyEnum.TOUGHNESS) && !poison)
    damage /= 2;

  var player = playerOne.characters.includes(character.characterId)
    ? playerOne
    : playerTwo;

  if (!poison) {
    if (
      character.properties.includes(propertyEnum.BABYSITTER) &&
      getChance(character, matchconfig.babysitterSuccessChance)
    ) {
      // babysitter
      var neighbors = [];
      getNeighbors(character.coordinates).forEach((neighbor) => {
        let neighborChar = getCharacterByPosition(neighbor);
        if (
          neighborChar &&
          player.characters.includes(neighborChar.characterId)
        ) {
          neighbors.push(neighborChar);
        }
      });
      if (neighbors.length > 0) {
        character =
          neighbors[Math.round(Math.random() * (neighbors.length - 1))];
        successful = false;
      }
    }
  }

  if (!npcCharacters.includes(character.characterId))
    player.totalDamage += damage;
  character.hp -= damage;

  if (character.hp <= 0) {
    // Exfiltration

    // Drop Collar
    var collar = getGadget(character, gadgetEnum.DIAMOND_COLLAR);
    if (collar) {
      map[character.coordinates.y][character.coordinates.x].gadget = collar;
      removeGadget(character, gadgetEnum.DIAMOND_COLLAR);
    }

    var seats = [];
    var freeSeats = [];
    for (let row in map) {
      for (let col in row) {
        let field = map[row][col];
        if ((field.state = fieldStateEnum.BAR_SEAT)) {
          if (!getCharacterByPosition({ x: col, y: row }))
            freeSeats.push({ x: col, y: row });
          seats.push({ x: col, y: row });
        }
      }
    }
    var randomSeat;
    if (freeSeats.length == 0) {
      randomSeat = seats[Math.round(Math.random() * (seats.length - 1))];
      getCharacterByPosition(randomSeat).coordinates = getRandomFreeNeighbor(
        randomSeat
      ); // move character
    } else {
      randomSeat =
        freeSeats[Math.round(Math.random() * (freeSeats.length - 1))];
    }

    latestOperations.push({
      type: operationEnum.EXFILTRATION,
      successful: true,
      target: randomSeat,
      characterId: character.characterId,
      from: character.coordinates,
    });
    character.coordinates = randomSeat;
    character.hp = 1;
  }
  return successful;
};

/**
 * Adds intelligence points to a character.
 * @param {Character} character The character to damage.
 * @param {Integer} ip The amaount of intelligence points.
 */
giveCharIP = (character, ip) => {
  character.ip += ip;

  // if wiretap with earplugs is active
  if (wireTapChar && earPlugsChar) {
    if (wireTapChar == character.characterId) {
      gameCharacters[earPlugsChar].ip += ip;
    }
  }
};

/**
 * Returns specific gadget of a character.
 * @param {Character} character The character.
 * @param {gadgetEnum} gadget The gadget type.
 * @return {Gadget} The gadget if. (Can be null if not found).
 */
getGadget = (char, type) => {
  for (let gadget of char.gadgets) {
    if (gadget.gadget == type) {
      return gadget;
    }
  }
  return null;
};

/**
 * Removes a specific gadget from a character.
 * @param {Character} character The character to damage.
 * @param {gadgetEnum} type The gadget type.
 */
removeGadget = (char, type) => {
  char.gadgets = char.gadgets.filter((gadget) => gadget.gadget !== type);
};

/**
 * Removes a specific property from a character.
 * @param {Character} character The character to damage.
 * @param {propertyEnum} type The property type.
 */
removeProperty = (char, type) => {
  char.properties = char.properties.filter((prop) => prop !== type);
};

/**
 * Returns the distance of to points.
 * @param {Point} pos1 The first point.
 * @param {Point} pos2 The second point.
 * @return {Integer} The distance of those two points.
 */
getDistance = (pos1, pos2) => {
  console.log(
    "distance ",
    pos1,
    pos2,
    Math.max(Math.abs(pos2.x - pos1.x), Math.abs(pos2.y - pos1.y))
  );
  return Math.max(Math.abs(pos2.x - pos1.x), Math.abs(pos2.y - pos1.y));
};

/**
 * Returns the nearest character to a specific point.
 * @param {Point} coordinates The point.
 * @param {Integer} maxDist The maximum distance.
 * @return {Character} The nearest character.
 */
getNearestCharacter = (coordinates, maxDist) => {
  var minDist = maxDist ? maxDist : gameCharacters[Object.keys(gameCharacters)[0]].coordinates;
  var minChar = null;
  for (let char of Object.values(gameCharacters)) {
    let dist = getDistance(coordinates, char.coordinates);
    if (dist <= minDist) {
      minDist = dist;
      minChar = char;
    }
  }
  return minChar;
};

/**
 * Returns if one point is a neighbor of the other.
 * @param {Point} position The position.
 * @param {Point} target The target position.
 * @param {Integer} [range=1] The range of neighborhood.
 * @return {Boolean} If they are neighbors.
 */
isNeighbor = (position, target, range = 1) => {
  return getDistance(position, target) == range;
};

/**
 * Returns all neighbors for a specific point.
 * @param {Point} coordinates The position.
 * @param {Integer} [range=1] The range of neighborhood.
 * @return {Point[]} Array of neighbors.
 */
getNeighbors = (coordinates, range = 1) => {
  var neighbors = [];
  for (let y = -range; y <= range; y++) {
    for (let x = -range; x <= range; x++) {
      let coords = { x: coordinates.x + x, y: coordinates.y + y };
      if (
        isCoordinatesValid(coords) &&
        coords.x != coordinates.x &&
        coords.y != coordinates.y
      ) {
        neighbors.push(coords);
      }
    }
  }
  return neighbors;
};

/**
 * Returns a random free neighbor for a specific point.
 * @param {Point} coordinates The position.
 * @param {Integer} [range=1] The range of neighborhood.
 * @return {Point} Random free neighbor.
 */
getRandomFreeNeighbor = (coordinates, range) => {
  var freeNeighbors = [];
  var neighbors = getNeighbors(coordinates, range);
  neighbors.forEach((coords) => {
    let field = map[coords.y][coords.x];
    if (field.state == fieldStateEnum.FREE && !getCharacterByPosition(coords))
      freeNeighbors.push(coords);
  });

  if (freeNeighbors.length > 0) {
    return freeNeighbors[
      Math.round(Math.random() * (freeNeighbors.length - 1))
    ];
  } else {
    return getRandomFreeNeighbor(
      neighbors[Math.round(Math.random() * (neighbors.length - 1))],
      range
    );
  }
};

/**
 * Returns a random neighbor character for a specific point.
 * @param {Point} coordinates The position.
 * @param {Integer} [range=1] The range of neighborhood.
 * @param {Boolean} [sight=false] If character has to be in sight (important for range > 1).
 * @return {Character} Random neighbor character.
 */
getRandomNeighborCharacter = (coordinates, range = 1, sight = false) => {
  var freeNeighbors = [];
  var neighbors = getNeighbors(coordinates, range);
  neighbors.forEach((coords) => {
    let field = map[coords.y][coords.x];
    if (field.state == fieldStateEnum.FREE) {
      let fieldChar = getCharacterByPosition(coords);
      if (fieldChar) {
        if (sight ? isInSight(coordinates, coords) : true) {
          freeNeighbors.push(fieldChar);
        }
      }
    }
  });

  if (freeNeighbors.length > 0) {
    return freeNeighbors[
      Math.round(Math.random() * (freeNeighbors.length - 1))
    ];
  } else {
    return getRandomNeighborCharacter(
      neighbors[Math.round(Math.random() * (neighbors.length - 1))],
      range,
      sight
    );
  }
};

/**
 * Returns a random character on the map.
 * @return {Character} Random character.
 */
getRandomCharacter = () => {
  var characters = [];
  for (let row in map) {
    for (let col in row) {
      let fieldChar = getCharacterByPosition({ x: col, y: row });
      if (fieldChar) characters.push(fieldChar);
    }
  }

  if (characters.length > 0) {
    return characters[Math.round(Math.random() * (characters.length - 1))];
  } else {
    return null;
  }
};

/**
 * Return if two points are in sight.
 * @param {Point} position The position.
 * @param {Point} target The target position.
 * @param {Boolean} chars If character block the sight.
 * @returns {Boolean} If in sight.
 */
isInSight = (position, target, chars = false) => {
  var x0 = position.x,
    y0 = position.y;
  var x1 = target.x,
    y1 = target.y;

  var dx = Math.abs(x1 - x0); // https://stackoverflow.com/questions/4672279/bresenham-algorithm-in-javascript
  var dy = Math.abs(y1 - y0);
  var sx = x0 < x1 ? 1 : -1;
  var sy = y0 < y1 ? 1 : -1;
  var err = dx - dy;

  while (true) {
    if (x0 === x1 && y0 === y1) break;

    let field = map[y0][x0];
    
    if (
      field.state == fieldStateEnum.WALL ||
      field.state == fieldStateEnum.FIREPLACE ||
      field.isFoggy ||
      chars
        ? getCharacterByPosition({ x: x0, y: y0 })
        : false
    ) {
      return false;
    }

    let e2 = 2 * err;
    if (e2 > -dy) {
      err -= dy;
      x0 += sx;
    }
    if (e2 < dx) {
      err += dx;
      y0 += sy;
    }
  }
  return true;
};

/**
 * If point is on the map.
 * @param {Point} coordinates The coordinates.
 * @returns {Boolean} If is on map.
 */
isCoordinatesValid = (coordinates) => {
  const { x, y } = coordinates;
  return y < map.length && y >= 0 && x < map[0].length && x >= 0;
};

/**
 * Perfoms actions which a character can do with certain properties.
 * @param {Player} player The player.
 * @param {Operation} operation The operation.
 * @returns {Boolean} If operation was valid.
 */
doOperation = (player, operation) => {
  const {
    type,
    successful,
    target,
    characterId,
    gadget,
    stake,
    usedProperty,
    from,
  } = operation;

  if (argv.verbose)
    console.info(characterId, " does ", operation, activeCharacterId);

  if (characterId == activeCharacterId) {
    const char = gameCharacters[characterId];

    //validization
    if (!char || (player && !player.characters.includes(characterId))) {
      player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
        reason: errorEnum.ILLEGAL_MESSAGE,
        debugMessage: "Not a valid character",
      });
      return false;
    }
    if (!isCoordinatesValid(target)) {
      player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
        reason: errorEnum.ILLEGAL_MESSAGE,
        debugMessage: "Target is not on map",
      });
      return false;
    }

    const targetField = map[target.y][target.x];
    const targetChar = getCharacterByPosition(targetField);

    operation.successful = true;

    /**
     * Performs property action which a character can do.
     * @param {Player} player The player.
     * @param {Character} char The player's character.
     * @param {Point} target The target.
     * @param {fieldStateEnum} targetField The target field.
     * @param {Character} targetChar The taget character (if on target field).
     * @param {propertyEnum} usedProperty The used property.
     * @param {operationEnum} operation The operation.
     * @return {Boolean} If action was valid.
     */
    doPropertyAction = (
      player,
      char,
      target,
      targetField,
      targetChar,
      usedProperty,
      operation
    ) => {
      switch (usedProperty) {
        case propertyEnum.BANG_AND_BURN: {
          if (
            !validizeNeighbor(target) ||
            !validizeFieldState(fieldStateEnum.ROULETTE_TABLE)
          )
            return false;
          targetField.isDestroyed = true; //the roulette Table is not playable
          targetField.isUpdated = true;
          break;
        }
        case propertyEnum.OBSERVATION: {
          if (!validizeSight(target, targetChar)) return false;

          if (
            getChance(char, matchconfig.observationSuccessChance) &&
            !player.characters.includes(targetChar.characterId)
          ) {
            if (
              targetChar.gadgets.includes(gadgetEnum.POCKET_LITTER) ||
              npcCharacters.includes(targetChar.characterId)
            ) {
              operation.isEnemy = false;
            } else {
              operation.isEnemy = true;
            }
          }
          break;
        }
      }

      latestOperations.push(operation);
      char.ap--; // Decrease action points
      return true;
    };

    /**
     * Perfoms spy on NPCs or on a safe.
     * @param {Player} player The player.
     * @param {Character} char The player's character.
     * @param {Point} target The target
     * @param {Character} targetChar The taget character (if on target field).
     * @param {operationEnum} operation The operation.
     * @return {Boolean} If action was valid.
     */
    doSpyAction = (player, char, target, targetChar, operation) => {
      if (!validizeTargetChar()) return false;
      // Flaps and Seals
      if (
        char.properties.includes(propertyEnum.FLAPS_AND_SEALS)
          ? !validizeNeighbor(target, 2)
          : !validizeNeighbor(target)
      )
        return false;

      // Spy on character
      if (targetChar) {
        if (npcCharacters.includes(targetChar.characterId)) {
          // if npc
          if (getChance(char, matchconfig.spySuccessChance)) {
            var secret =
              safeCombinations[
                Math.round(Math.random() * (safeCombinations.length - 1))
              ]; // get random safe combination
            if (player.safeCombinations.includes(secret)) {
              giveCharIP(char, matchconfig.secretToIpFactor); // add IPs if secret was unknown
            }
            player.safeCombinations.push(secret); // add secret
          }
        } else {
          operation.isEnemy = true;
          operation.successful = false;
        }
      } else {
        // Spy on safe
        var secret =
          safeCombinations[
            Math.round(Math.random() * (safeCombinations.length - 1))
          ]; // get random safe combination
        if (player.safeCombinations.includes(secret)) {
          giveCharIP(char, matchconfig.secretToIpFactor); // add IPs if secret was unknown
        }
        player.safeCombinations.push(secret); // add secret

        if (
          targetField.gadget &&
          targetField.gadget.gadget == gadgetEnum.DIAMOND_COLLAR
        ) {
          char.gadgets.push(targetField.gadget);
        }
      }

      char.ap--;
      latestOperations.push(operation);
      return true;
    };

    /**
     * Perform move to a specific character.
     * @param {Player} player The player.
     * @param {Point} target The target.
     * @param {Character} char The player's character.
     * @param {Character} targetChar The taget character. (if on target field)
     * @param {operationEnum} operation The operation.
     * @return {Boolean} If action was valid.
     */
    doMovementAction = (player, target, char, targetChar, operation) => {
      if (!validizeNeighbor(target)) return false;
      if (argv.verbose) console.info(char, " moves to ", target);

      // extra validization
      if (
        targetField.state != fieldStateEnum.FREE &&
        targetField.state != fieldStateEnum.BAR_SEAT
      ) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Target is not free or a bar seat",
        });
        return false;
      }

      // take gadget
      if (targetField.gadget) {
        char.gadgets.push(targetField.gadget);
      }

      // give collar to cat
      if (target.x == catCoordinates.x && target.y == catCoordinates.y) {
        giveCharIP(char, matchconfig.catIp);
        catHandover = player;
        endGame();
      }

      if (targetChar) targetChar.coordinates = {x: char.coordinates.x, y: char.coordinates.y}; // switch places
      char.coordinates = {x: target.x, y: target.y}; // move to target
      char.mp--; // decrease move points

      latestOperations.push(operation);
      return true;
    };

    /**
     * Perfroms roulette/gamble action.
     * @param {Player} player The player.
     * @param {Point} target The target.
     * @param {Character} char The player's character.
     * @param {operationEnum} stake The amount of stake.
     * @param {fieldStateEnum} targetField The target field.
     * @param {operationEnum} operation The operation.
     * @return {Boolean} If action was valid.
     */
    doGambleAction = (player, target, char, stake, targetField, operation) => {
      if (
        !validizeFieldState(fieldStateEnum.ROULETTE_TABLE) ||
        !validizeNeighbor(target) ||
        !validizeStake(stake)
      )
        return false;

      var probability = 18 / 37;

      if (targetField.chipAmount > 0) {
        switch (char.properties) {
          case propertyEnum.LUCKY_DEVIL: {
            probability = 23 / 37;
            break;
          }
          case propertyEnum.JINX: {
            probability = 13 / 37;
            break;
          }
        }

        if (
          char.properties.includes(propertyEnum.CLAMMY_CLOTHES) ||
          char.properties.includes(propertyEnum.CONSTANT_CLAMMY_CLOTHES)
        )
          probability /= 2;

        if (
          targetField.isInverted
            ? Math.random() > probability
            : Math.random() <= probability
        ) {
          char.chips += stake;
          targetField.chipAmount = 0;
          targetField.isUpdated = true;
        } else {
          targetField.chipAmount += stake;
          targetField.isUpdated = true;
          char.chips -= stake;
        }
      } else {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Operation is not valid",
        });
      }
      char.ap--; // Decrease action points

      latestOperations.push(operation);
      return true;
    };

    /**
     * Performs gadget action for a specfic character.
     * @param {Player} player The player.
     * @param {Character} char The player's character.
     * @param {Point} target The target.
     * @param {Character} targetChar The taget character. (if on target field)
     * @param {operationEnum} operation The operation.
     * @param {fieldStateEnum} targetField The target field.
     * @param {operationEnum} operation The operation.
     * @return {Boolean} If action was valid.
     */
    doGadgetAction = (
      player,
      gadget,
      target,
      char,
      targetField,
      targetChar,
      operation
    ) => {
      switch (gadget) {
        case gadgetEnum.COCKTAIL: {
          if (
            target.x == char.coordinates.x &&
            target.y == char.coordinates.y
          ) {
            if (!validizeGadget(gadgetEnum.COCKTAIL) || !validizeTargetChar())
              return false;

            // drink
            var cocktailHp = matchconfig.cocktailHp;
            if (char.properties.includes(propertyEnum.ROBUST_STOMACH))
              cocktailHp *= 2;

            if (getGadget(char, gadgetEnum.COCKTAIL).isPoisoned) {
              operation.successful = damageCharacter(char, cocktailHp, true);
            } else {
              char.hp += cocktailHp;
            }
            player.cocktrailDrinks++;
            removeGadget(char, gadgetEnum.COCKTAIL); // remove gadget
          } else {
            if (!validizeNeighbor(target) || !validizeSight(target))
              return false;

            if (targetField.state == fieldStateEnum.BAR_TABLE) {
              // take
              char.gadgets.push({
                gadget: gadgetEnum.COCKTAIL,
                usages: 0,
              });
              targetField.gadget = null;
              targetField.isUpdated = true;
            } else {
              // spill
              if (!validizeGadget(gadgetEnum.COCKTAIL) || !validizeTargetChar())
                return false;

              if (
                targetChar.properties.includes(propertyEnum.HONEY_TRAP) &&
                getChance(character, matchconfig.honeyTrapSuccessChance)
              ) {
                // honey trap
                var nTargetChar = getRandomNeighborCharacter(char.coordinates);
                if (nTargetChar) targetChar = nTargetChar;
              }

              if (getChance(char, matchconfig.cocktailDodgeChance)) {
                targetChar.properties.push(propertyEnum.CLAMMY_CLOTHES); // dodge
                operation.successful = false;
                player.cocktailSpills++;
              }
              removeGadget(char, gadgetEnum.COCKTAIL); // remove gadget
            }
          }
          break;
        }
        case gadgetEnum.HAIRDRYER: {
          if (!validizeGadget(gadgetEnum.HAIRDRYER)) return false;

          if (targetField.state == fieldStateEnum.BAR_TABLE) {
            if (!validizeNeighbor(target) || !validizeTargetChar())
              return false;

            removeProperty(targetChar, propertyEnum.CLAMMY_CLOTHES);
          } else {
            removeProperty(char, propertyEnum.CLAMMY_CLOTHES);
          }
          getGadget(char, gadgetEnum.HAIRDRYER).usages++;
          break;
        }
        case gadgetEnum.MOLEDIE: {
          if (
            !validizeGadget(gadgetEnum.MOLEDIE) ||
            !validizeDistance(target, matchconfig.moledieRange) ||
            !validizeSight(target)
          )
            return false;

          // get back deactivated properties
          char.properties.push(...char.deactivatedProperties);
          char.deactivatedProperties = [];

          // bounce off
          if (!targetChar) targetChar = getNearestCharacter(target);

          // throw to other character and deactivate properties
          targetChar.gadgets.push({
            gadget: gadgetEnum.MOLEDIE,
            usages: 0,
          });
          var newProps = [];
          for (let prop of targetChar.properties) {
            if (
              prop == propertyEnum.TRADECRAFT ||
              prop == propertyEnum.FLAPS_AND_SEALS ||
              prop == propertyEnum.OBSERVATION
            ) {
              targetChar.deactivatedProperties.push(prop);
            } else {
              newProps.push(prop);
            }
          }
          targetChar.properties = newProps;
          break;
        }
        case gadgetEnum.TECHNICOLOUR_PRISM: {
          if (
            !validizeGadget(gadgetEnum.TECHNICOLOUR_PRISM) ||
            !validizeNeighbor(target) ||
            !validizeFieldState(fieldStateEnum.ROULETTE_TABLE)
          )
            return false;

          targetField.isInverted = true;
          targetField.isUpdated = true;
          removeGadget(char, gadgetEnum.TECHNICOLOUR_PRISM);
          break;
        }
        case gadgetEnum.BOWLER_BLADE: {
          if (
            !validizeGadget(gadgetEnum.BOWLER_BLADE) ||
            !validizeDistance(target, matchconfig.bowlerBladeRange) ||
            !validizeSight(target, true)
          )
            return false;

          if (
            targetChar.properties.includes(propertyEnum.HONEY_TRAP) &&
            getChance(character, matchconfig.honeyTrapSuccessChance)
          ) {
            // honey trap
            var nTargetChar = getRandomNeighborCharacter(
              char.coordinates,
              matchconfig.bowlerBladeRange,
              true
            );
            if (nTargetChar) targetChar = nTargetChar;
          }

          // Try to hit target character
          if (!getGadget(targetChar, gadgetEnum.MAGNETIC_WATCH)) {
            if (getChance(char, matchconfig.bowlerBladeHitChance)) {
              damageCharacter(targetChar, matchconfig.bowlerBladeDamage);
            } else {
              operation.successful = false;
            }
          }

          getRandomFreeNeighbor(targetChar.coordinates).gadget =
            gadgetEnum.BOWLER_BLADE; // lay bowler blade on random free neighbor
          break;
        }
        case gadgetEnum.POISON_PILLS: {
          if (!validizeGadget(gadgetEnum.POISON_PILLS)) return false;

          if (targetChar) {
            if (!validizeNeighbor(target) || !validizeTargetChar(target))
              return false;

            // extra validization
            var targetGadget = getGadget(targetChar, gadgetEnum.COCKTAIL);
            if (!targetGadget) {
              player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
                reason: errorEnum.ILLEGAL_MESSAGE,
                debugMessage: "Target character has no cocktail",
              });
              return false;
            }

            targetGadget.isPoisoned = true; //Poison Cocktail
          } else {
            if (
              !validizeNeighbor(target) ||
              !validizeFieldState(fieldStateEnum.BAR_TABLE) ||
              !validizeFieldGadget(gadgetEnum.COCKTAIL)
            )
              return false;

            targetField.gadget.isPoisoned = true; //Poison Cocktail
            targetField.isUpdated = true;
          }

          var charGadget = getGadget(char, gadgetEnum.POISON_PILLS);
          charGadget.usages++;

          //check if all Pills are used
          if (charGadget.usages == 5) {
            removeGadget(char, gadgetEnum.POISON_PILLS); // remove gadget
          }
          break;
        }
        case gadgetEnum.LASER_COMPACT: {
          if (
            !validizeGadget(gadgetEnum.LASER_COMPACT) ||
            !validizeSight(target)
          )
            return false;

          // extra validization
          if (targetField.state != fieldStateEnum.BAR_TABLE && !targetChar) {
            player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Target is neither bar table nor a character",
            });
            return false;
          }

          if (targetChar) {
            if (!getGadget(targetChar)) {
              player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
                reason: errorEnum.ILLEGAL_MESSAGE,
                debugMessage: "Target character does not have a cocktail",
              });
              return false;
            }
            if (getChance(char, matchconfig.laserCompactHitChance)) {
              removeGadget(targetChar, gadgetEnum.COCKTAIL);
            } else {
              operation.successful = false;
            }
          } else {
            if (targetField.gadget.gadget != gadgetEnum.COCKTAIL) {
              player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
                reason: errorEnum.ILLEGAL_MESSAGE,
                debugMessage: "Bar table does not have a cocktail",
              });
              return false;
            }
            if (getChance(char, matchconfig.laserCompactHitChance)) {
              targetField.gadget = null;
              targetField.isUpdated = true;
            } else {
              operation.successful = false;
            }
          }
          break;
        }
        case gadgetEnum.ROCKET_PEN: {
          if (!validizeGadget(gadgetEnum.ROCKET_PEN) || !validizeSight(target))
            return false;

          if (targetField.state == fieldStateEnum.WALL) {
            targetField.state = fieldStateEnum.FREE; // destroy target wall
            targetField.isUpdated = true;
          }
          if (targetChar) {
            if (
              targetChar.properties.includes(propertyEnum.HONEY_TRAP) &&
              getChance(character, matchconfig.honeyTrapSuccessChance)
            ) {
              // honey trap
              var nTargetChar = getRandomCharacter(true);
              if (nTargetChar) targetChar = nTargetChar;
            }
            damageCharacter(targetChar, matchconfig.rocketPenDamage); // deal damage to taget character
          }

          getNeighbors(target).forEach((coords) => {
            let field = map[coords.y][coords.x];
            if (field.state == fieldStateEnum.WALL) {
              //destroy neighbor walls
              field.state = fieldStateEnum.FREE;
            }
            let fieldChar = getCharacterByPosition(coords);
            if (fieldChar) {
              if (
                fieldChar.properties.includes(propertyEnum.HONEY_TRAP) &&
                getChance(fieldChar, matchconfig.honeyTrapSuccessChance)
              ) {
                // honey trap
                var nFieldChar = getRandomNeighborCharacter(coords);
                if (nFieldChar) fieldChar = nFieldChar;
              }
              damageCharacter(fieldChar, matchconfig.rocketPenDamage); // deal damage to character
            }
          });

          removeGadget(char, gadgetEnum.ROCKET_PEN); // remove gadget
          break;
        }
        case gadgetEnum.GAS_GLOSS: {
          if (
            !validizeGadget(gadgetEnum.GAS_GLOSS) ||
            !validizeNeighbor(target) ||
            !validizeTargetChar()
          )
            return false;

          if (
            targetChar.properties.includes(propertyEnum.HONEY_TRAP) &&
            getChance(character, matchconfig.honeyTrapSuccessChance)
          ) {
            // honey trap
            var nTargetChar = getRandomNeighborCharacter(char.coordinates);
            if (nTargetChar) targetChar = nTargetChar;
          }

          damageCharacter(targetChar, matchconfig.gasGlossDamage); //deal damage to target character
          removeGadget(char, gadgetEnum.GAS_GLOSS); // remove gadget
          return true;
        }
        case gadgetEnum.MOTHBALL_POUCH: {
          if (
            !validizeGadget(gadgetEnum.MOTHBALL_POUCH) ||
            !validizeDistance(target, matchconfig.mothballPouchRange) ||
            !validizeSight(target) ||
            !validizeFieldState(fieldStateEnum.FIREPLACE)
          )
            return false;

          getNeighbors(target).forEach((coords) => {
            let neighbor = getCharacterByPosition(coords);
            if (neighbor) {
              if (
                neighbor.properties.includes(propertyEnum.HONEY_TRAP) &&
                getChance(neighbor, matchconfig.honeyTrapSuccessChance)
              ) {
                // honey trap
                var nNeighbor = getRandomNeighborCharacter(coords);
                if (nNeighbor) neighbor = nNeighbor;
              }
              damageCharacter(neighbor, matchconfig.mothballPouchDamage);
            }
          });

          var charGadget = getGadget(char, gadgetEnum.MOTHBALL_POUCH);
          charGadget.usages++;

          //check if all moth balls are used
          if (charGadget.usages == 5) {
            removeGadget(char, gadgetEnum.MOTHBALL_POUCH); // remove gadget
          }
          break;
        }
        case gadgetEnum.FOG_TIN: {
          if (
            !validizeGadget(gadgetEnum.FOG_TIN) ||
            !validizeSight(target) ||
            !validizeDistance(target, matchconfig.fogTinRange)
          )
            return false;

          // extra validization
          if (targetField.state == fieldStateEnum.WALL) {
            player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Target can not be a " + fieldState,
            });
            return false;
          }

          targetField.isFoggy = true;
          targetField.isUpdated = true;
          getNeighbors(target).forEach((field) => {
            field.isFoggy = true;
            field.isUpdated = true;
          });
          fogTinIsActive = true;
          fogTin = target;
          break;
        }
        case gadgetEnum.GRAPPLE: {
          if (
            !validizeGadget(gadgetEnum.GRAPPLE) ||
            !validizeDistance(target, matchconfig.grappleRange) ||
            !validizeSight(target)
          )
            return false;

          // extra validization
          if (
            targetField.state != fieldStateEnum.FREE &&
            targetField.state != fieldStateEnum.BAR_TABLE
          ) {
            player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
              reason: errorEnum.ILLEGAL_MESSAGE,
              debugMessage: "Target is not free or a bar table",
            });
            return false;
          }

          if (getChance(char, matchconfig.grappleHitChance)) {
            char.gadgets.push(targetField.gadget); // add gadget to character
            targetField.gadget = null; // remove gadget from field
            targetField.isUpdated = true;
          } else {
            operation.successful = false;
          }
          break;
        }
        case gadgetEnum.JETPACK: {
          if (
            !validizeGadget(gadgetEnum.JETPACK) ||
            !validizeFieldState(fieldStateEnum.FREE) ||
            validizeTargetChar(target) // character on field
          )
            return false;

          char.coordinates = target; // move to target
          removeGadget(char, gadgetEnum.JETPACK);
          break;
        }
        case gadgetEnum.WIRETAP_WITH_EARPLUGS: {
          if (!validizeTargetChar() || !validizeNeighbor(target)) return false;

          getGadget(char).activeOn = targetChar;
          wireTapChar = targetChar.characterId;
          earPlugsChar = char.characterId;
          break;
        }
        case gadgetEnum.CHICKEN_FEED: {
          if (
            !validizeTargetChar() ||
            !validizeGadget(gadgetEnum.CHICKEN_FEED) ||
            !validizeNeighbor(target)
          )
            return false;

          if (!player.characters.includes(targetChar.characterId)) {
            if (char.ip > targetChar.ip) {
              giveCharIP(char, char.ip - targetChar.ip);
            } else if (char.ip < targetChar.ip) {
              giveCharIP(targetChar, targetChar.ip - char.ip);
            }
          } else {
            operation.successful = false;
          }
          removeGadget(char, gadgetEnum.CHICKEN_FEED);
          break;
        }
        case gadgetEnum.NUGGET: {
          if (
            !validizeGadget(gadgetEnum.NUGGET) ||
            !validizeTargetChar() ||
            !validizeNeighbor(target)
          )
            return false;

          if (npcCharacters.includes(targetChar.characterId)) {
            // if npc add to players characters
            player.characters.push(targetChar.characterId);
            player.equipment[targetChar.characterId] = targetChar;
            removeGadget(gadgetEnum.NUGGET);
          } else {
            // else give nugget to target character
            targetChar.gadgets.push(getGadget(char, gadgetEnum.NUGGET));
          }

          break;
        }
        case gadgetEnum.MIRROR_OF_WILDERNESS: {
          if (!validizeTargetChar() || !validizeNeighbor(target)) return false;

          if (player.characters.includes(targetChar.characterId)) {
            // exchange ips
            var tempIp = char.ip;
            char.ip = targetChar.ip;
            targetChar.ip = tempIp;
          } else {
            // opponent
            if (getChance(char, matchconfig.mirrorSwapChance)) {
              // exchange ips
              var tempIp = char.ip;
              char.ip = targetChar.ip;
              targetChar.ip = tempIp;

              removeGadget(char, gadgetEnum.MIRROR_OF_WILDERNESS); // remove gadget from character
            } else {
              operation.successful = false;
            }
          }
          break;
        }
        default: {
          player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
            reason: errorEnum.ILLEGAL_MESSAGE,
            debugMessage: "Incorrect operation",
          });
          return false;
        }
      }

      latestOperations.push(operation);
      char.ap--; // Decrease action points
      return true;
    };

    // Validization

    /**
     * Validizies targetChar
     * @return If valid.
     */
    validizeTargetChar = () => {
      if (!targetChar) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Target has no character",
        });
        return false;
      }
      return true;
    };

    /**
     * Validizies gadget
     * @param {gadgetEnum} gadget The gadget.
     * @return If valid.
     */
    validizeGadget = (gadget) => {
      if (!getGadget(char, gadget)) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Character does not own gadget " + gadget,
        });
        return false;
      }
      return true;
    };

    /**
     * Validizes distance between current position and target.
     * @param {Point} target The target.
     * @param {number} maxDist The maximum distance.
     * @return If valid.
     */
    validizeDistance = (target, maxDist) => {
      if (getDistance(char.coordinates, target) > maxDist) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Target out of range",
        });
        return false;
      }
      return true;
    };

    /**
     * Validizes sight between current position and target.
     * @param {Point} target The target.
     * @param {Boolean} chars If characters block the sight.
     * @return If valid.
     */
    validizeSight = (target, chars) => {
      if (!isInSight(char.coordinates, target, chars)) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Target is not in line of sight",
        });
        return false;
      }
      return true;
    };

    /**
     * Validizes if field is in specific state.
     * @param {fieldStateEnum} fieldState The field state.
     * @return If valid.
     */
    validizeFieldState = (fieldState) => {
      if (targetField.state != fieldState) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Target is not a " + fieldState,
        });
        return false;
      }
      return true;
    };

    /**
     * Validizes if gadget is of specific type.
     * @param {gadgetEnum} gadget The gadget.
     * @return Is valid.
     */
    validizeFieldGadget = (gadget) => {
      if (targetField.gadget.gadget != gadget) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Field has no " + gadget,
        });
        return false;
      }
    };

    /**
     * Validizes if target is neighbor of current position.
     * @param {Point} target The target.
     * @param {Integer} [range=1] The range.
     * @return Is valid.
     */
    validizeNeighbor = (target, range = 1) => {
      if (!isNeighbor(char.coordinates, target, range)) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Target is not a neighbor",
        });
        return false;
      }
      return true;
    };

    /**
     * Validize stake.
     * @param {Integer} stake The stake.
     * @return Is valid.
     */
    validizeStake = (stake) => {
      if (stake > targetField.chipAmount) {
        player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
          reason: errorEnum.ILLEGAL_MESSAGE,
          debugMessage: "Stake is bigger than chip amount",
        });
        return false;
      }
      return true;
    };

    // Performs operation
    switch (type) {
      case operationEnum.GADGET_ACTION: {
        if (char.ap == 0) {
          player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
            reason: errorEnum.ILLEGAL_MESSAGE,
            debugMessage: "No action points left",
          });
          return false;
        }
        return doGadgetAction(
          player,
          gadget,
          target,
          char,
          targetField,
          targetChar,
          operation
        );
      }
      case operationEnum.MOVEMENT: {
        if (char.mp == 0) {
          player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
            reason: errorEnum.ILLEGAL_MESSAGE,
            debugMessage: "No move points left",
          });
          return false;
        }
        return doMovementAction(player, target, char, targetChar, operation);
      }
      case operationEnum.GAMBLE_ACTION: {
        if (char.ap == 0) {
          player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
            reason: errorEnum.ILLEGAL_MESSAGE,
            debugMessage: "No action points left",
          });
          return false;
        }
        return doGambleAction(
          player,
          target,
          char,
          stake,
          targetField,
          operation
        );
      }
      case operationEnum.PROPERTY_ACTION: {
        if (char.ap == 0) {
          player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
            reason: errorEnum.ILLEGAL_MESSAGE,
            debugMessage: "No action points left",
          });
          return false;
        }
        return doPropertyAction(
          player,
          char,
          target,
          targetField,
          targetChar,
          usedProperty,
          operation
        );
      }
      case operationEnum.SPY_ACTION: {
        if (char.ap == 0) {
          player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
            reason: errorEnum.ILLEGAL_MESSAGE,
            debugMessage: "No action points left",
          });
          return false;
        }
        return doSpyAction(player, target, char, targetChar, operation);
      }
    }
  } else {
    // close connection if not valid
    player.sendMessage(messageTypeEnum.ILLEGAL_MESSAGE, {
      reason: errorEnum.ILLEGAL_MESSAGE,
      debugMessage: "Operation is not valid",
    });
    player.disqualifyPlayer();
  }
};
