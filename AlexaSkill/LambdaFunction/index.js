const AWS = require('aws-sdk');
AWS.config.update({ region: 'us-east-1' });

const encrypted_aws_access_key = process.env['MY_AWS_ACCESS_KEY'];
let deycrypted_aws_access_key;

const encrypted_aws_secret_key = process.env['MY_AWS_SECRET_KEY'];
let deycrypted_aws_secret_key;
//config.log('Initial encrypted: ', encrypted_endpoint);


var config =  {};
config.IOT_BROKER_ENDPOINT      = process.env.IOT_BROKER_ENDPOINT;
config.IOT_BROKER_REGION        = process.env.IOT_BROKER_REGION;
config.IOT_THING_NAME           = process.env.IOT_THING_NAME;
config.AWS_ACCESS_KEY           = "";
config.AWS_SECRET_KEY           = "";
config.FRIENDLY_NAME            = "Rope Lights";

//Loading AWS SDK libraries
AWS.config.region = config.IOT_BROKER_REGION;
AWS.config.accessKeyId = config.AWS_ACCESS_KEY;
AWS.config.secretAccessKey = config.AWS_SECRET_KEY;

//Initializing client for IoT
var iotData = new AWS.IotData({endpoint: config.IOT_BROKER_ENDPOINT});

function onSessionStarted(sessionStartedRequest, session) {
    console.log(`onSessionStarted requestId=${sessionStartedRequest.requestId}, sessionId=${session.sessionId}`);
}

function onLaunch(launchRequest, session, callback) {
    console.log(`onLaunch requestId=${launchRequest.requestId}, sessionId=${session.sessionId}`);
    getWelcomeResponse(callback);
}

function getWelcomeResponse(callback) {
    const sessionAttributes = {};
    const cardTitle = 'Welcome';
    const speechOutput = 'Welcome to Rope Lights. You may turn the lights on or off, set the color, or subscribe to a sports team.' ;

    const repromptText = 'You can say: what switch to control ' ;
    const shouldEndSession = false;
    callback(sessionAttributes,
        buildSpeechletResponse(cardTitle, speechOutput, repromptText, shouldEndSession));
}

function handleSessionEndRequest(callback) {
    const cardTitle = 'Session Ended';
    const speechOutput = 'Thank you for using rope lights, have a great day!';
    const shouldEndSession = true;
    callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
}


function onSessionEnded(sessionEndedRequest, session) {
    console.log(`onSessionEnded requestId=${sessionEndedRequest.requestId}, sessionId=${session.sessionId}`);
    // Add cleanup logic here
}

function turnOnLights (callback) {
    //Set the pump to 1 for activation on the device
    var values = {
        "power": "on",
        "duration": -1
    };
    var payloadObj={
        "state": {
            "desired": values
        }
    };


    //Prepare the parameters of the update call
    var paramsUpdate = {
        "thingName" : config.IOT_THING_NAME,
        "payload" : JSON.stringify(payloadObj)
    };

    console.log("Pushing to thing:", JSON.stringify(paramsUpdate));
    //Update Device Shadow
    iotData.updateThingShadow(paramsUpdate, function(err, data) {
        log("Shadow response", data);
        var cardTitle = 'Turned on Rope Lights';
        var speechOutput = "The lights are on!";
        if(err || data === null) {
            cardTitle = 'Failed to turn on Rope Lights';
            speechOutput = "We were unable to turn on the Rope Lights";
            log("Lights Error: ", JSON.stringify(err));
        }
        const shouldEndSession = true;
        callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
    });
}

function turnOffLights (callback) {
    //Set the pump to 1 for activation on the device
    log("DEBUG", "Turning off lights");
    var vals = {
        "power": "off",
        "duration": -1
    };

    var payloadObj = {
        "state": {
            "desired": vals
        }
    };


    console.log("Thing name: ", config.IOT_THING_NAME);
    //Prepare the parameters of the update call
    var paramsUpdate = {
        "thingName" : config.IOT_THING_NAME,
        "payload" : JSON.stringify(payloadObj)
    };

    console.log("Pushing to thing:", JSON.stringify(paramsUpdate));
    //Update Device Shadow
    return iotData.updateThingShadow(paramsUpdate, function(err, data) {
        console.log("Shadow response", data);
        var cardTitle = 'Turned off Rope Lights';
        var speechOutput = "The lights are off!";
        if(err || data === null) {
            cardTitle = 'Failed to turn off Rope Lights';
            speechOutput = "We were unable to turn off the Rope Lights";
            log("Lights Error: ", JSON.stringify(err));
        }
        const shouldEndSession = true;
        callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
    });
}

function setBrightness(value, callback) {
    log("DEBUG", "Setting brightness to ", value);
    var vals = {
        "power": "on",
        "brightness": value
    };

    var payloadObj = {
        "state": {
            "desired": vals
        }
    };


    //Prepare the parameters of the update call
    var paramsUpdate = {
        "thingName" : config.IOT_THING_NAME,
        "payload" : JSON.stringify(payloadObj)
    };

    console.log("Pushing to thing:", JSON.stringify(paramsUpdate));
    //Update Device Shadow
    return iotData.updateThingShadow(paramsUpdate, function(err, data) {
        console.log("Shadow response", data);
        var cardTitle = 'Adjusted brightness to ' + value;
        var speechOutput = "The brightness has been set to " + value;
        if(err || data === null) {
            cardTitle = 'Failed to set brightness';
            speechOutput = "We were unable to adjust the brightness of the Rope Lights";
            log("Lights Error: ", JSON.stringify(err));
        }
        const shouldEndSession = true;
        callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
    });
}

function setColor (color, callback) {
    //Set the pump to 1 for activation on the device
    var values = {
        "power": "on",
        "colors": [
            createColorFromString(color)
        ],
    };
    var payloadObj={
        "state": {
            "desired": values
        }
    };


    //Prepare the parameters of the update call
    var paramsUpdate = {
        "thingName" : config.IOT_THING_NAME,
        "payload" : JSON.stringify(payloadObj)
    };

    console.log("Pushing to thing:", JSON.stringify(paramsUpdate));
    //Update Device Shadow
    iotData.updateThingShadow(paramsUpdate, function(err, data) {
        log("Shadow response", data);
        var cardTitle = 'Set Color';
        var speechOutput = "The color is set to " + color;
        if(err || data === null) {
            cardTitle = 'Failed to set color';
            speechOutput = "We were unable to set the color of the Rope Lights";
            log("Lights Error: ", JSON.stringify(err));
        }
        const shouldEndSession = true;
        callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
    });
}

function subscribe(team, teamName, callback) {
    console.log("Subscribing to the ", teamName);
    var params = {
        thingName: config.IOT_THING_NAME
    };
    iotData.getThingShadow(params, function(err, data) {
        var cardTitle, speechOutput;
        if (err) {
            console.log(err, err.stack);
            cardTitle = 'Connection Error';
            speechOutput = "We were unable to connect to the Rope Lights";
            const shouldEndSession = true;
            callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
            return;
        }
        else{
            var current = JSON.parse(data.payload);
            var teams = current.state.reported.teams;
            if(teams == null) {
                teams = [team];
            }
            else if(teams.includes(team)) {
                cardTitle = 'Already subscribed';
                speechOutput = "You are already subscribed to the ".concat(teamName);
                const shouldEndSession = true;
                callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
                return;
            }
            else
                teams.push(team);

            var values = {
                "teams": teams
            };
            var payloadObj={
                "state": {
                    "desired": values
                }
            };

            //Prepare the parameters of the update call
            var paramsUpdate = {
                "thingName" : config.IOT_THING_NAME,
                "payload" : JSON.stringify(payloadObj)
            };

            console.log("Pushing to thing:", JSON.stringify(paramsUpdate));
            //Update Device Shadow
            iotData.updateThingShadow(paramsUpdate, function(err, data) {
                log("Shadow response", data);
                var cardTitle = 'Subscribed';
                var speechOutput = "You are now subscribed to the ".concat(teamName);
                if(err || data === null) {
                    cardTitle = 'Failed to subscribe';
                    speechOutput = "We were unable subscribe to the ".concat(teamName);
                    log("Lights Error: ", JSON.stringify(err));
                }
                const shouldEndSession = true;
                callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
            });
        }
    });
}

function unsubscribe(team, teamName, callback) {
    console.log("Unsubscribing to the ", teamName);
    var params = {
        thingName: config.IOT_THING_NAME
    };
    iotData.getThingShadow(params, function(err, data) {
        var cardTitle, speechOutput;
        if (err) {
            console.log(err, err.stack);
            cardTitle = 'Connection';
            speechOutput = "We were unable to connect to the Rope Lights";
            const shouldEndSession = true;
            callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
            return;
        }
        else{
            var current = JSON.parse(data.payload);
            var teams = current.state.reported.teams;
            console.log("Starting Teams: ", JSON.stringify(teams));
            if(teams == null || !teams.includes(team)) {
                cardTitle = 'Not subscribed';
                speechOutput = "You are not subscribed to the ".concat(teamName);
                const shouldEndSession = true;
                callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
                return;
            }
            else {
                for( var i = teams.length - 1; i >= 0; i--){
                    if ( teams[i] === team) {
                        teams.splice(i, 1);
                    }
                }
            }

            console.log("Ending Teams: ", JSON.stringify(teams));
            var values = {
                "teams": teams
            };
            var payloadObj={
                "state": {
                    "desired": values
                }
            };

            //Prepare the parameters of the update call
            var paramsUpdate = {
                "thingName" : config.IOT_THING_NAME,
                "payload" : JSON.stringify(payloadObj)
            };

            console.log("Pushing to thing:", JSON.stringify(paramsUpdate));
            //Update Device Shadow
            iotData.updateThingShadow(paramsUpdate, function(err, data) {
                log("Shadow response", data);
                var cardTitle = 'Unsubscribed';
                var speechOutput = "You are now unsubscribed from the ".concat(teamName);
                if(err || data === null) {
                    cardTitle = 'Failed to unsubscribe';
                    speechOutput = "We were unable unsubscribe from the ".concat(teamName);
                    log("Unsubscription Error: ", JSON.stringify(err));
                }
                const shouldEndSession = true;
                callback({}, buildSpeechletResponse(cardTitle, speechOutput, null, shouldEndSession));
            });
        }
    });
}

function log(message, message1, message2) {
    console.log(message + message1 + message2);
}

function buildSpeechletResponse(title, output, repromptText, shouldEndSession) {
    return {
        outputSpeech: {
            type: 'PlainText',
            text: output,
        },
        card: {
            type: 'Simple',
            title: `${config.FRIENDLY_NAME} - ${title}`,
            content: output,
        },
        reprompt: {
            outputSpeech: {
                type: 'PlainText',
                text: repromptText,
            },
        },
        shouldEndSession,
    };
}

function buildResponse(sessionAttributes, speechletResponse) {
    return {
        version: '1.0',
        sessionAttributes,
        response: speechletResponse,
    };
}

function onIntent(intentRequest, session, callback) {
    console.log(`onIntent requestId=${intentRequest.requestId}, sessionId=${session.sessionId}, intent=${intentRequest.intent}`);
    const intent = intentRequest.intent;
    const intentName = intentRequest.intent.name;
    // Dispatch to your skill's intent handlers
    if (intentName === 'AMAZON.HelpIntent') {
        getWelcomeResponse(callback);
    } else if (intentName === 'AMAZON.StopIntent') {
        handleSessionEndRequest(callback);
    } else if (intentName.toLowerCase() === 'power') {
        var lightstatus = intent.slots.powerState.value;
        if(lightstatus.toLowerCase() === 'on')
            turnOnLights(callback);
        else
            turnOffLights(callback);
    } else if(intentName.toLowerCase() === 'setbrightness') {
        setBrightness(Number.parseInt(intent.slots.value.value, 10), callback);
    } else if(intentName.toLowerCase() === 'setcolor') {
        setColor(intent.slots.color.value, callback);
    } else if(intentName.toLowerCase() === 'subscribe') {
        subscribe(Number.parseInt(intent.slots.team.resolutions.resolutionsPerAuthority[0].values[0].value.id, 10), intent.slots.team.value, callback);
    } else if(intentName.toLowerCase() === 'unsubscribe') {
        unsubscribe(Number.parseInt(intent.slots.team.resolutions.resolutionsPerAuthority[0].values[0].value.id, 10), intent.slots.team.value, callback);
    }
}

exports.handler = (event, context, callback) => {
    if (!(deycrypted_aws_access_key && deycrypted_aws_secret_key)) {
        // Decrypt code should run once and variables stored outside of the function
        // handler so that these are decrypted once per container
        const kms = new AWS.KMS();
        const decryptionPromises =[
            kms.decrypt({ CiphertextBlob: new Buffer(encrypted_aws_access_key, 'base64') }).promise(),
            kms.decrypt({ CiphertextBlob: new Buffer(encrypted_aws_secret_key, 'base64') }).promise()
        ];

        Promise.all( decryptionPromises ).then( data => {
            deycrypted_aws_access_key = data[0].Plaintext.toString('ascii');
			deycrypted_aws_secret_key = data[1].Plaintext.toString('ascii');
		}).catch( err => {
            console.log('Error while decrypting:', err);
			return callback(err);
		});
    }
	
    config.AWS_ACCESS_KEY = deycrypted_aws_access_key;
    config.AWS_SECRET_KEY = deycrypted_aws_secret_key;
    AWS.config.accessKeyId = config.AWS_ACCESS_KEY;
    AWS.config.secretAccessKey = config.AWS_SECRET_KEY;

    try {
        console.log(`event.session.application.applicationId=${event.session.application.applicationId}`);

        if (event.session.new) {
            onSessionStarted({ requestId: event.request.requestId }, event.session);
        }
        if (event.request.type === 'LaunchRequest') {
            onLaunch(event.request,
                event.session,
                (sessionAttributes, speechletResponse) => {
                callback(null, buildResponse(sessionAttributes, speechletResponse));
        });
        } else if (event.request.type === 'IntentRequest') {
            onIntent(event.request,
                event.session,
                (sessionAttributes, speechletResponse) => {
                callback(null, buildResponse(sessionAttributes, speechletResponse));
        });
        } else if (event.request.type === 'SessionEndedRequest') {
            onSessionEnded(event.request, event.session);
            callback();
        }
    } catch (err) {
        log("Error:", err);
        callback(err);
    }
};


class Color {
    constructor(r, g, b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    static constrain(val) {
        if(val < 0)
            return 0;
        else if(val > 255)
            return 255;
        else
            return val;
    }
}

function createColorFromString(s) {
    if(s.toLowerCase() === "red")
        return new Color(255, 0, 0);
    else if(s.toLowerCase() === "orange")
        return new Color(255, 165, 0);
    else if(s.toLowerCase() === "yellow")
        return new Color(255, 255, 0);
    else if(s.toLowerCase() === "green")
        return new Color(0, 255, 0);
    else if(s.toLowerCase() === "blue")
        return new Color(0, 0, 255);
    else if(s.toLowerCase() === "purple")
        return new Color(160, 32, 240);
    else if(s.toLowerCase() === "white")
        return new Color(255, 255, 255);
}