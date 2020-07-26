const WebSocket = require("ws");
var dateFormat = require("dateformat");
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
} = require("./enums");
const yargs = require("yargs");
const argv = yargs
  .option("role", {
    alias: "r",
    description: "Your role",
  })
  .help()
  .alias("help", "h").argv;

var ws = new WebSocket("ws://localhost:7007");

var sessionId;

startConnection = () => {
  ws.on("open", () => {
    console.log("opened");

    if (sessionId) {
      ws.sendMessage(messageTypeEnum.RECONNECT, {
        sessionId,
      });
    } else {
      ws.sendMessage(messageTypeEnum.HELLO, {
        name: "Frank",
        role: "PLAYER",//argv.role,
      });
    }
  });

  ws.on("message", (message) => {
    const jsonMessage = JSON.parse(message);
    console.log(jsonMessage);

    const { clientId, type, creationDate, debugMessage } = jsonMessage;

    switch (type) {
      case messageTypeEnum.HELLO_REPLY: {
        sessionId = jsonMessage.sessionId;
        level = jsonMessage.level;
        settings = jsonMessage.settings;
        characterSettings = jsonMessage.characterSettings;
        break;
      }
      case messageTypeEnum.REQUEST_ITEM_CHOICE: {
        const { offeredCharacterIds, offeredGadgets } = jsonMessage;
        var response = {};
        if (offeredCharacterIds.length == 0) {
          //Gadget
          response.chosenGadget =
            offeredGadgets[
              Math.round(Math.random() * (offeredGadgets.length - 1))
            ];
        } else if (offeredGadgets.length == 0) {
          //Character
          response.chosenCharacterId =
            offeredCharacterIds[
              Math.round(Math.random() * (offeredCharacterIds.length - 1))
            ];
        } else if (Math.round(Math.random())) {
          //Character
          response.chosenCharacterId =
            offeredCharacterIds[
              Math.round(Math.random() * (offeredCharacterIds.length - 1))
            ];
        } else {
          //Gadget
          response.chosenGadget =
            offeredGadgets[
              Math.round(Math.random() * (offeredGadgets.length - 1))
            ];
        }

        setTimeout(() => {
          ws.sendMessage(messageTypeEnum.ITEM_CHOICE, response);
        }, 500);
        break;
      }
      case messageTypeEnum.REQUEST_EQUIPMENT_CHOICE: {
        const { chosenCharacterIds, chosenGadgets } = jsonMessage;
        var equipment = {};
        chosenCharacterIds.forEach((char) => {
          if (chosenGadgets.length > 0) {
            var r = Math.round(Math.random() * (chosenGadgets.length - 1));
            if (equipment[char]) {
              equipment[char].push(chosenGadgets[r]);
            } else {
              equipment[char] = [chosenGadgets[r]];
            }
            chosenGadgets.splice(r, 1);
          }
        });

        setTimeout(() => {
          ws.sendMessage(messageTypeEnum.EQUIPMENT_CHOICE, { equipment });
        }, 500);
        break;
      }
    }
  });

  ws.sendMessage = (type, data, debugMessage = "") => {
    ws.send(
      JSON.stringify({
        clientId: ws.clientId,
        type,
        creationDate: dateFormat(new Date(), "dd.mm.yyyy hh:MM:ss"),
        debugMessage,
        ...data,
      })
    );
  };
};

startConnection();

var standard_input = process.stdin;
standard_input.setEncoding("utf-8");
standard_input.on("data", (msg) => {
  if (msg.startsWith("disconnect")) {
    ws.close();
  } else if (msg.startsWith("reconnect")) {
    ws = new WebSocket("ws://localhost:8080");
    startConnection();
  }
});
