var exec = require("cordova/exec");

/**
 * Constructor.
 *
 * @returns {Tangova}
 */
function Tangova() { }

/**
 * Starts the tango motion tracking and depth camera
 */
Tangova.prototype.startTango = function (successCallback, errorCallback) {
    if (errorCallback == null) {
        errorCallback = function () {
        };
    }

    if (typeof errorCallback != "function") {
        console.log("Tangova.startTango: failure parameter not a function");
        return;
    }

    if (typeof successCallback != "function") {
        console.log("Tangova.startTango: success callback parameter must be a function");
        return;
    }

    exec(successCallback, errorCallback, 'Tangova', 'start_tango', []);
}

/**
 * Stops the tango motion tracking and depth camera
 */
Tangova.prototype.stopTango = function (successCallback, errorCallback) {
    if (errorCallback == null) {
        errorCallback = function () {
        };
    }

    if (typeof errorCallback != "function") {
        console.log("Tangova.stopTango: failure parameter not a function");
        return;
    }

    if (typeof successCallback != "function") {
        console.log("Tangova.stopTango: success callback parameter must be a function");
        return;
    }

    exec(successCallback, errorCallback, 'Tangova', 'stop_tango', []);
}

/**
 * Requests a depth frame
 */
Tangova.prototype.requestDepthFrame = function (successCallback, errorCallback) {
    if (errorCallback == null) {
        errorCallback = function () {
        };
    }

    if (typeof errorCallback != "function") {
        console.log("Tangova.requestDepthFrame: failure parameter not a function");
        return;
    }

    if (typeof successCallback != "function") {
        console.log("Tangova.requestDepthFrame: success callback parameter must be a function");
        return;
    }

    exec(successCallback, errorCallback, 'Tangova', 'request_depth_frame', []);
}

module.exports = new Tangova();
